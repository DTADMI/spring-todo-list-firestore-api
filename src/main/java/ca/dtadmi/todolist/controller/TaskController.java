package ca.dtadmi.todolist.controller;

import ca.dtadmi.todolist.entity.BaseTask;
import ca.dtadmi.todolist.entity.Task;
import ca.dtadmi.todolist.entity.TaskResult;
import ca.dtadmi.todolist.exceptions.FirestoreExcecutionException;
import ca.dtadmi.todolist.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/todolist/tasks")
public class TaskController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private TaskResult paginateResults(String page, String limit, boolean showMetadata, List<Task> tasks) {
        List<Task> resultsData = new ArrayList<>(tasks);
        int numberTasks = resultsData.size();
        TaskResult results = new TaskResult();
        results.setData(new ArrayList<>());

        if("0".equals(page)) {
            logger.debug("Pages start at 1. Rectifying the page number from 0 to 1");
            page = "1";
        }
        if(!page.isBlank() && !limit.isBlank()) {
            logger.debug("Getting sliced tasks with {} elements starting at page {}", limit, page);
            int perPage = Integer.parseInt(limit);
            int pageCount = Math.ceilDivExact(numberTasks, perPage);
            int limitInt = pageCount == 0 ? 1 : Math.min(perPage, numberTasks);
            int pageInt = pageCount == 0 ? 1 : Math.min(Integer.parseInt(page), pageCount);
            int startIndex = (pageInt - 1) * limitInt;
            int endIndex = (pageInt) * limitInt;
            logger.debug("Getting sliced tasks from {} to {} excluded", startIndex, endIndex);

            if(showMetadata) {
                results.setMetadata(new TaskResult.Metadata(pageInt, perPage, pageCount, numberTasks, new EnumMap<>(TaskResult.Metadata.LinksKeys.class)));
                String linkUriFormat = "/api/todolist/tasks?page=%d&limit=%d";
                results.getMetadata().getLinks().put(TaskResult.Metadata.LinksKeys.SELF, String.format(linkUriFormat, pageInt, limitInt));
                results.getMetadata().getLinks().put(TaskResult.Metadata.LinksKeys.FIRST, String.format("/api/todolist/tasks?page=1&limit=%d", limitInt));
                results.getMetadata().getLinks().put(TaskResult.Metadata.LinksKeys.LAST, String.format(linkUriFormat, (pageCount!=0) ? pageCount : 1, limitInt));

                if(startIndex > 0) {
                    results.getMetadata().getLinks().put(TaskResult.Metadata.LinksKeys.PREVIOUS, String.format(linkUriFormat, pageInt - 1, limitInt));
                }
                if(endIndex < numberTasks) {
                    results.getMetadata().getLinks().put(TaskResult.Metadata.LinksKeys.NEXT, String.format(linkUriFormat, pageInt + 1, limitInt));
                }
            }
            resultsData = resultsData.subList(startIndex, endIndex);
        }

        resultsData.forEach(task -> task.setUri(String.format("/api/todolist/tasks/%s", task.getId())));
        results.setData(resultsData);
        return results;
    }

    @GetMapping("")
    public ResponseEntity<TaskResult> getTasks(@RequestParam(required = false, defaultValue = "") String page, @RequestParam(required = false, defaultValue = "") String limit, @RequestParam(required = false, defaultValue = "true") boolean showMetadata) {
        try {
            TaskResult results;
            List<Task> tasks =  taskService.findAll();
            if(tasks.isEmpty()){
                logger.debug("No result found");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            results = paginateResults(page, limit, showMetadata, tasks);
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        try {
            if(id.isBlank()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Task result = taskService.findById(id);
            if(result == null) {
                logger.debug("No result found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Task> getTaskByName(@PathVariable String name) {
        try {
            if(name.isBlank()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Task result = taskService.findByName(name);
            if(result == null) {
                logger.debug("No result found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/task")
    public ResponseEntity<Task> createTask(@RequestBody BaseTask task) {
        try {
            if(task==null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Task result = taskService.create(task);

            if(result == null) {
                logger.debug("Error while creating task");
                return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("")
    public ResponseEntity<TaskResult> getAllUserTasks(@RequestParam(required = false, defaultValue = "") String page, @RequestParam(required = false, defaultValue = "") String limit, @RequestParam(required = false, defaultValue = "true") boolean showMetadata, @RequestBody String userId) {
        try {
            if(userId.isBlank()){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            TaskResult results;
            List<Task> tasks =  taskService.findAllFromUser(userId);
            if(tasks.isEmpty()){
                logger.debug("No results found");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            results = paginateResults(page, limit, showMetadata, tasks);
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("")
    public ResponseEntity<Task> updateTask(@RequestBody Task taskUpdate) {
        try {
            if(taskUpdate==null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Task result = taskService.update(taskUpdate);

            if(result == null) {
                logger.debug("Error while updating task");
                return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/subtasks")
    public ResponseEntity<Task> updateSubtasks(@RequestBody Task taskUpdate) {
        try {
            if(taskUpdate==null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Task result = taskService.updateSubtasks(taskUpdate);

            if(result == null) {
                logger.debug("Error while updating task's subtasks");
                return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Task> deleteTaskById(@PathVariable String id) {
        try {
            if(id.isBlank()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            taskService.remove(id);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<Task> deleteTaskByName(@RequestParam(required = true, defaultValue = "") String name) {
        try {
            if(name.isBlank()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Task taskToDelete = taskService.findByName(name);
            taskService.remove(taskToDelete.getId());
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (FirestoreExcecutionException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
