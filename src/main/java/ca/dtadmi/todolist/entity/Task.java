package ca.dtadmi.todolist.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseTask {

    @NonNull
    private String id;
    private String uri;

    public Task(String id){
        super();
        this.id = id;
    }

    public Task(BaseTask baseTask) {
        super(baseTask);
    }
}
