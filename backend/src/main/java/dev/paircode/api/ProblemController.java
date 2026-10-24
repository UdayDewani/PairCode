package dev.paircode.api;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/v1/problems")
class ProblemController {
  private final ProblemRepository problems; ProblemController(ProblemRepository problems){this.problems=problems;}
  @GetMapping Map<String,Object> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) String search){
    page=Math.max(0,page); size=Math.min(Math.max(1,size),100);
    Pageable pageable=PageRequest.of(page,size,Sort.by("number"));
    Page<Problem> result;
    if(search!=null&&!search.isBlank()) result=problems.findByTitleContainingIgnoreCaseOrTopicContainingIgnoreCase(search,search,pageable);
    else result=problems.findAll(pageable);
    return Map.of("items",result.getContent().stream().map(Api::problem).toList(),"page",page,"size",size,"total",result.getTotalElements());
  }
  @GetMapping("/{id}") Map<String,Object> detail(@PathVariable UUID id){return problems.findById(id).map(Api::problem).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Problem not found"));}
}
