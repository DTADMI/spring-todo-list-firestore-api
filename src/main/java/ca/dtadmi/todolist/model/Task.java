package ca.dtadmi.todolist.model;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Task extends BaseTask {

    private String id;

    public Task(String id){
        super();
        this.id = id;
    }

    public Task(BaseTask baseTask) {
        super(baseTask);
    }
}
