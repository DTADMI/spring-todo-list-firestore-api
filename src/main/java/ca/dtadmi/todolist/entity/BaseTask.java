package ca.dtadmi.todolist.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseTask {

    @NonNull
    private String name;
    @NonNull
    private Boolean isDone;
    @NonNull
    private String creationDate;
    private String dueDate;
    private List<String> subtasks;
    private String superTask;
    private String lastModificationDate;
    @NonNull
    private String userId;

    public BaseTask(String name, Boolean isDone, String creationDate, String userId){
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
