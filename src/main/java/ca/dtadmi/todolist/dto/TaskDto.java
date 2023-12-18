package ca.dtadmi.todolist.dto;

import ca.dtadmi.todolist.entity.TaskEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDto {
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
    @NonNull
    private String uri;

    public TaskDto(@NonNull String id, @NonNull String name, @NonNull Boolean isDone, @NonNull String creationDate, @NonNull String userId, @NonNull String uri){
        this.id = id;
        this.name = name;
        this.isDone = isDone;
        this.creationDate = creationDate;
        this.userId = userId;
        this.uri = uri;
    }

    public TaskDto(TaskEntity baseTaskEntity) {
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
