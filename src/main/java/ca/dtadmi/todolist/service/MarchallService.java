package ca.dtadmi.todolist.service;

import ca.dtadmi.todolist.entity.Task;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarchallService {

    public static Optional<Task> unSerialize(Map<?, ?> serializedData){
        if(serializedData == null){
            return Optional.empty();
        }
        Task task = new Task();
        task.setId((String) serializedData.get("id"));
        task.setName((String) serializedData.get("name"));

        if(serializedData.get("uri")!=null){
            task.setUri((String) serializedData.get("uri"));
        }
        if(serializedData.get("isDone")!=null){
            task.setIsDone((Boolean) serializedData.get("isDone"));
        }
        if(serializedData.get("creationDate")!=null){
            task.setCreationDate((String) serializedData.get("creationDate"));
        }
        if(serializedData.get("dueDate")!=null){
            task.setDueDate((String) serializedData.get("dueDate"));
        }
        if(serializedData.get("userId")!=null){
            task.setUserId((String) serializedData.get("userId"));
        }
        if(serializedData.get("lastModificationDate")!=null){
            task.setLastModificationDate((String) serializedData.get("lastModificationDate"));
        }
        if(serializedData.get("superTask")!=null){
            task.setSuperTask((String) serializedData.get("superTask"));
        }
        Object serializedSubtasks = serializedData.get("subtasks");
        if(serializedSubtasks instanceof Collection<?>){
            List<String> subtasks = ((List<Object>) serializedSubtasks)
                    .stream().map(o -> (String)o).toList();
            task.setSubtasks(subtasks);
        }

        return Optional.of(task);
    }
}
