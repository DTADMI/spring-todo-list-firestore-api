package ca.dtadmi.todolist.controller;

import ca.dtadmi.todolist.dto.TaskDto;
import ca.dtadmi.todolist.dto.TaskResultDto;
import ca.dtadmi.todolist.entity.TaskEntity;
import ca.dtadmi.todolist.exceptions.FirestoreExcecutionException;
import ca.dtadmi.todolist.model.Task;
import ca.dtadmi.todolist.service.CachingService;
import ca.dtadmi.todolist.service.MarshallService;
import ca.dtadmi.todolist.service.TaskService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/todolist/tasks")
@CacheConfig(cacheNames = "tasks")
public class TaskController {

    private static final String GET_ALL_USER_TASKS = "getAllUserTasks";
    public static final String NO_RESULT_FOUND = "No result found";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TaskService taskService;
    private final CachingService cachingService;

    private static final String CACHE_NAME = "tasks";
    private static final String ALL_TASKS_CACHE_KEY = "allTasks";
    private static final String USER_TASKS_CACHE_KEY = "allUserTasks";

    public TaskController(TaskService taskService, CachingService cachingService) {
        this.taskService = taskService;
        this.cachingService = cachingService;
    }

    @GetMapping("")
    public ResponseEntity<TaskResultDto> getTasks(@RequestParam(defaultValue = "1") String page, @RequestParam(defaultValue = "1") String limit, @RequestParam(required = false, defaultValue = "true") boolean showMetadata) {
        try {
            TaskResultDto results = (TaskResultDto) cachingService.getSingleCacheValue(CACHE_NAME, ALL_TASKS_CACHE_KEY);
            if(results == null) {//no cached value found
                List<Task> tasks =  taskService.findAll();
                if(tasks.isEmpty()){
                    logger.debug(NO_RESULT_FOUND);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                results = MarshallService.paginateResults(page, limit, showMetadata, tasks);
                cachingService.cacheSingleValue(CACHE_NAME, ALL_TASKS_CACHE_KEY, results);
            }
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @GetMapping("{id}")
    @Cacheable(key = "#id", unless="#result == null")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable String id) {
        try {
            if(id.isBlank()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            TaskDto result = MarshallService.modelToDto(taskService.findById(id));
            if(result == null) {
                logger.debug(NO_RESULT_FOUND);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @GetMapping("/name/{name}")
    @Cacheable(key = "#name", unless="#result.statusCodeValue == 404")
    //@CacheEvict(key = "#name", condition = "#result.statusCodeValue == 404")
    public ResponseEntity<TaskDto> getTaskByName(@PathVariable String name) {
        try {
            if(name.isBlank()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            TaskDto result = MarshallService.modelToDto(taskService.findByName(name));
            if(result == null) {
                logger.debug(NO_RESULT_FOUND);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @PostMapping("/task")
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskEntity taskEntity) {
        try {
            if(taskEntity ==null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            TaskResultDto cachedResults = ((TaskResultDto)cachingService.getSingleCacheValue(CACHE_NAME, ALL_TASKS_CACHE_KEY));
            List<TaskDto> allTasks = (cachedResults == null || cachedResults.getData().isEmpty()) ? taskService.findAll().stream().map(MarshallService::modelToDto).toList() : cachedResults.getData();
            if(!allTasks.isEmpty() && allTasks.stream().anyMatch(taskDto -> taskDto.getName().equals(taskEntity.getName()))){
                logger.error("Existing name: please choose a unique name for the task");
                return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
            }
            TaskDto result = MarshallService.modelToDto(taskService.create(taskEntity));

            if(result == null) {
                logger.error("Error while creating task");
                return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
            }
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @PostMapping("")
    @CircuitBreaker(name= GET_ALL_USER_TASKS, fallbackMethod = "getAllUserTasksFallBack")
    @Retry(name=GET_ALL_USER_TASKS, fallbackMethod = "getAllUserTasksFallBack")
    @RateLimiter(name=GET_ALL_USER_TASKS, fallbackMethod = "getAllUserTasksRateLimiterFallBack")
    @TimeLimiter(name=GET_ALL_USER_TASKS)
    public ResponseEntity<TaskResultDto> getAllUserTasks(@RequestParam(defaultValue = "1") String page, @RequestParam(defaultValue = "1") String limit, @RequestParam(required = false, defaultValue = "true") boolean showMetadata, @RequestBody TaskEntity baseTask) {
        try {
            if(baseTask==null || baseTask.getUserId().isBlank()){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            String userId = baseTask.getUserId();
            TaskResultDto results = (TaskResultDto) cachingService.getSingleCacheValue(CACHE_NAME, USER_TASKS_CACHE_KEY);
            if(results == null) {//no cached value found
                List<Task> tasks =  taskService.findAllFromUser(userId);
                if(tasks.isEmpty()){
                    logger.debug("No results found");
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                results = MarshallService.paginateResults(page, limit, showMetadata, tasks);
                cachingService.cacheSingleValue(CACHE_NAME, USER_TASKS_CACHE_KEY, results);
            }
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @PutMapping("")
    @CachePut(key = "#taskUpdate.id")
    public ResponseEntity<TaskDto> updateTask(@RequestBody Task taskUpdate) {
        try {
            if(taskUpdate == null || taskUpdate.getId() == null || taskUpdate.getName() == null || taskUpdate.getIsDone() == null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            TaskDto result = MarshallService.modelToDto(taskService.update(taskUpdate));

            if (result == null) {
                logger.error("No such record found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            cachingService.evictSingleCacheValue(CACHE_NAME, ALL_TASKS_CACHE_KEY);
            cachingService.evictSingleCacheValue(CACHE_NAME, USER_TASKS_CACHE_KEY);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            logger.error("Error while updating task");
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @PutMapping("/subtasks")
    @CachePut(key = "#taskEntityUpdate.id")
    public ResponseEntity<TaskDto> updateSubtasks(@RequestBody Task taskEntityUpdate) {
        try {
            if(taskEntityUpdate ==null || taskEntityUpdate.getId() == null || taskEntityUpdate.getName() == null || taskEntityUpdate.getIsDone() == null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            TaskDto result = MarshallService.modelToDto(taskService.updateSubtasks(taskEntityUpdate));

            if (result == null) {
                logger.error("No such record found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            cachingService.evictSingleCacheValue(CACHE_NAME, ALL_TASKS_CACHE_KEY);
            cachingService.evictSingleCacheValue(CACHE_NAME, USER_TASKS_CACHE_KEY);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @DeleteMapping("/deleteAll")
    @CacheEvict(allEntries = true)
    public ResponseEntity<TaskEntity> deleteTasks() {
        try {
            taskService.removeAll();
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @DeleteMapping("{id}")
    @CacheEvict(key = "#id")
    public ResponseEntity<TaskEntity> deleteTaskById(@PathVariable String id) {
        try {
            if(id.isBlank()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            taskService.remove(id);
            cachingService.evictSingleCacheValue(CACHE_NAME, ALL_TASKS_CACHE_KEY);
            cachingService.evictSingleCacheValue(CACHE_NAME, USER_TASKS_CACHE_KEY);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @DeleteMapping("")
    @CacheEvict(key = "#name")
    public ResponseEntity<TaskDto> deleteTaskByName(@RequestParam(defaultValue = "") String name) {
        try {
            if(name.isBlank()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Task taskToDelete = taskService.findByName(name);
            if(taskToDelete!=null) {
                taskService.remove(taskToDelete.getId());
                cachingService.evictSingleCacheValue(CACHE_NAME, ALL_TASKS_CACHE_KEY);
                cachingService.evictSingleCacheValue(CACHE_NAME, USER_TASKS_CACHE_KEY);
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    public ResponseEntity<String> getAllUserTasksFallBack(Exception e) {
        logger.error(e.getMessage(), e);
        return new ResponseEntity<>("The service is down. Please come back later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ResponseEntity<String> getAllUserTasksRateLimiterFallBack(Exception e) {
        logger.error(e.getMessage(), e);
        return new ResponseEntity<>("The service received an unusually large amount of requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }
}
