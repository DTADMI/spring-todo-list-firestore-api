package ca.dtadmi.todolist.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskEntity {

    @NonNull
    private String id;
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

    public TaskEntity(@NonNull String id, @NonNull String name, @NonNull Boolean isDone, @NonNull String creationDate, @NonNull String userId){
        this.id = id;
        this.name = name;
        this.isDone = isDone;
        this.creationDate = creationDate;
        this.userId = userId;
    }

    public TaskEntity(TaskEntity baseTaskEntity) {
        this.id = baseTaskEntity.getId();
        this.name = baseTaskEntity.getName();
        this.isDone = baseTaskEntity.getIsDone();
        this.creationDate = baseTaskEntity.getCreationDate();
        if(baseTaskEntity.getDueDate() != null){
            this.dueDate = baseTaskEntity.getDueDate();
        }
        if(baseTaskEntity.getSubtasks() != null){
            this.subtasks = new ArrayList<>(baseTaskEntity.getSubtasks());
        }
        if(baseTaskEntity.getSuperTask() != null){
            this.superTask = baseTaskEntity.getSuperTask();
        }
        if(baseTaskEntity.getLastModificationDate() != null){
            this.lastModificationDate = baseTaskEntity.getLastModificationDate();
        }
        this.userId = baseTaskEntity.getUserId();
    }
}
