package ca.dtadmi.todolist.service;

import ca.dtadmi.todolist.dto.TaskDto;
import ca.dtadmi.todolist.dto.TaskResultDto;
import ca.dtadmi.todolist.entity.TaskEntity;
import ca.dtadmi.todolist.model.Task;
import com.google.api.client.util.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MarshallService {
    private static final Logger logger = LoggerFactory.getLogger(MarshallService.class);

    private MarshallService() {
    }

    public static Task unSerialize(TaskEntity serializedData){
        if(serializedData == null){
            return null;
        }
        Task task = new Task();
        task.setId(serializedData.getId());
        task.setName(serializedData.getName());

        task.setIsDone(serializedData.getIsDone());
        task.setCreationDate(new DateTime((serializedData.getCreationDate())));
        if(serializedData.getDueDate()!=null){
            task.setDueDate(new DateTime(serializedData.getDueDate()));
        }
        task.setUserId(serializedData.getUserId());
        if(serializedData.getLastModificationDate()!=null){
            task.setLastModificationDate(new DateTime(serializedData.getLastModificationDate()));
        }
        if(serializedData.getSuperTask()!=null){
            task.setSuperTask(serializedData.getSuperTask());
        }
        if(serializedData.getSubtasks() != null){
            List<String> subtasks = new ArrayList<>(serializedData.getSubtasks());
            task.setSubtasks(subtasks);
        }

        return task;
    }

    public static TaskEntity serialize(Task data){
        if(data == null){
            return null;
        }
        TaskEntity taskEntity = new TaskEntity();

        taskEntity.setId(data.getId());
        taskEntity.setName(data.getName());
        taskEntity.setIsDone(data.getIsDone());
        taskEntity.setCreationDate(data.getCreationDate().toString());
        taskEntity.setUserId(data.getUserId());

        taskEntity.setDueDate(data.getDueDate() != null ? data.getDueDate().toString() : null);
        taskEntity.setLastModificationDate(data.getLastModificationDate() != null ? data.getLastModificationDate().toString() : null);
        taskEntity.setSuperTask(data.getSuperTask()!= null ? data.getSuperTask() : null);
        if(data.getSubtasks() != null){
            List<String> subtasks = new ArrayList<>(data.getSubtasks());
            taskEntity.setSubtasks(subtasks);
        }

        return taskEntity;
    }

    public static TaskEntity getTaskEntityFromFirestore(Map<?, ?> serializedData){
        if(serializedData == null){
            return null;
        }
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId((String) serializedData.get("id"));
        taskEntity.setName((String) serializedData.get("name"));

        taskEntity.setIsDone((Boolean) serializedData.get("isDone"));
        taskEntity.setCreationDate(((String) serializedData.get("creationDate")));
        if(serializedData.get("dueDate")!=null){
            taskEntity.setDueDate((String) serializedData.get("dueDate"));
        }
        taskEntity.setUserId((String) serializedData.get("userId"));
        if(serializedData.get("lastModificationDate")!=null){
            taskEntity.setLastModificationDate((String) serializedData.get("lastModificationDate"));
        }
        if(serializedData.get("superTask")!=null){
            taskEntity.setSuperTask((String) serializedData.get("superTask"));
        }
        Object serializedSubtasks = serializedData.get("subtasks");
        if(serializedSubtasks instanceof Collection<?>){
            List<String> subtasks = ((List<Object>) serializedSubtasks)
                    .stream().map(o -> (String)o).toList();
            taskEntity.setSubtasks(subtasks);
        }

        return taskEntity;
    }

    public static TaskDto entityToDto(TaskEntity taskEntity){
        if(taskEntity == null){
            return null;
        }
        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskEntity.getId());
        taskDto.setName(taskEntity.getName());

        taskDto.setIsDone(taskEntity.getIsDone());
        taskDto.setCreationDate(taskEntity.getCreationDate());
        if(taskEntity.getDueDate()!=null){
            taskDto.setDueDate(taskEntity.getDueDate());
        }
        taskDto.setUserId(taskEntity.getUserId());
        if(taskEntity.getLastModificationDate()!=null){
            taskDto.setLastModificationDate(taskEntity.getLastModificationDate());
        }
        if(taskEntity.getSuperTask()!=null){
            taskDto.setSuperTask(taskEntity.getSuperTask());
        }
        if(taskEntity.getSubtasks() != null){
            List<String> subtasks = new ArrayList<>(taskEntity.getSubtasks());
            taskDto.setSubtasks(subtasks);
        }

        return taskDto;
    }

    public static TaskDto modelToDto(Task task){
        if(task == null){
            return null;
        }
        TaskDto taskDto = new TaskDto();
        taskDto.setId(task.getId());
        taskDto.setName(task.getName());

        taskDto.setIsDone(task.getIsDone());
        taskDto.setCreationDate(task.getCreationDate().toString());
        if(task.getDueDate()!=null){
            taskDto.setDueDate(task.getDueDate().toString());
        }
        taskDto.setUserId(task.getUserId());
        if(task.getLastModificationDate()!=null){
            taskDto.setLastModificationDate(task.getLastModificationDate().toString());
        }
        if(task.getSuperTask()!=null){
            taskDto.setSuperTask(task.getSuperTask());
        }
        if(task.getSubtasks() != null){
            List<String> subtasks = new ArrayList<>(task.getSubtasks());
            taskDto.setSubtasks(subtasks);
        }
        taskDto.setUri(String.format("/api/todolist/tasks/%s", task.getId()));

        return taskDto;
    }

    public static TaskResultDto paginateResults(String page, String limit, boolean showMetadata, List<Task> tasks) {
        List<TaskDto> resultsData = new ArrayList<>(tasks.stream().map(MarshallService::modelToDto).toList());
        int numberTasks = resultsData.size();
        TaskResultDto results = new TaskResultDto();
        results.setData(new ArrayList<>());

        logger.debug("Getting sliced tasks with {} elements starting at page {}", limit, page);
        int perPage = Math.min(Integer.parseInt(limit), 6);
        int pageCount = Math.ceilDivExact(numberTasks, perPage);
        int limitInt = pageCount == 0 ? 1 : Math.min(perPage, numberTasks);
        int pageInt = pageCount == 0 ? 1 : Math.min(Integer.parseInt(page), pageCount);
        int startIndex = (pageInt - 1) * limitInt;
        int endIndex = (pageInt) * limitInt;
        logger.debug("Getting sliced tasks from {} to {} excluded", startIndex, endIndex);

        if(showMetadata) {
            results.setMetadata(new TaskResultDto.Metadata(pageInt, perPage, pageCount, numberTasks, new EnumMap<>(TaskResultDto.Metadata.LinksKeys.class)));
            String linkUriFormat = "/api/todolist/tasks?page=%d&limit=%d";
            results.getMetadata().getLinks().put(TaskResultDto.Metadata.LinksKeys.SELF, String.format(linkUriFormat, pageInt, limitInt));
            results.getMetadata().getLinks().put(TaskResultDto.Metadata.LinksKeys.FIRST, String.format("/api/todolist/tasks?page=1&limit=%d", limitInt));
            results.getMetadata().getLinks().put(TaskResultDto.Metadata.LinksKeys.LAST, String.format(linkUriFormat, (pageCount!=0) ? pageCount : 1, limitInt));

            if(startIndex > 0) {
                results.getMetadata().getLinks().put(TaskResultDto.Metadata.LinksKeys.PREVIOUS, String.format(linkUriFormat, pageInt - 1, limitInt));
            }
            if(endIndex < numberTasks) {
                results.getMetadata().getLinks().put(TaskResultDto.Metadata.LinksKeys.NEXT, String.format(linkUriFormat, pageInt + 1, limitInt));
            }
            resultsData = resultsData.subList(startIndex, endIndex);
        }

        results.setData(resultsData);
        return results;
    }
}
