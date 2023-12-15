package ca.dtadmi.todolist.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResult {

    private List<Task> data;
    @JsonProperty("_metadata")
    private Metadata metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private Integer page;
        @JsonProperty("per_page")
        private Integer perPage;
        @JsonProperty("page_count")
        private Integer pageCount;
        @JsonProperty("total_count")
        private Integer totalCount;
        @JsonProperty("Links")
        private Map<LinksKeys, String> links;



        @Getter
        public enum LinksKeys {
            @JsonProperty("self")
            SELF,
            @JsonProperty("first")
            FIRST,
            @JsonProperty("previous")
            PREVIOUS,
            @JsonProperty("next")
            NEXT,
            @JsonProperty("last")
            LAST;
        }

    }
}



