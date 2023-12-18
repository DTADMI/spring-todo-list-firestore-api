package ca.dtadmi.todolist.model;

import com.google.api.client.util.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseTask {

    private String name;
    private Boolean isDone;
    private DateTime creationDate;
    private DateTime dueDate;
    private List<String> subtasks;
    private String superTask;
    private DateTime lastModificationDate;
    private String userId;

    public BaseTask(String name, Boolean isDone, DateTime creationDate, String userId){
        this.name = name;
        this.isDone = isDone;
        this.creationDate = creationDate;
        this.userId = userId;
    }

    public BaseTask(BaseTask baseTask) {
        this.name = baseTask.getName();
        this.isDone = baseTask.getIsDone();
        this.creationDate = baseTask.getCreationDate();
        if(baseTask.getDueDate() != null){
            this.dueDate = baseTask.getDueDate();
        }
        if(baseTask.getSubtasks() != null){
            this.subtasks = new ArrayList<>(baseTask.getSubtasks());
        }
        if(baseTask.getSuperTask() != null){
            this.superTask = baseTask.getSuperTask();
        }
        if(baseTask.getLastModificationDate() != null){
            this.lastModificationDate = baseTask.getLastModificationDate();
        }
        this.userId = baseTask.getUserId();
    }
}
