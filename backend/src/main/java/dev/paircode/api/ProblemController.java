package dev.paircode.api;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/v1/problems")
class ProblemController {
  private final ProblemRepository problems; ProblemController(ProblemRepository problems){this.problems=problems;}
  @GetMapping Map<String,Object> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) String search,@RequestParam(required=false) String topic,@RequestParam(required=false) Difficulty difficulty){
    var all=problems.findAll(Sort.by("number"));var filtered=all.stream().filter(p->search==null||p.title.toLowerCase().contains(search.toLowerCase())||p.topic.toLowerCase().contains(search.toLowerCase())).filter(p->topic==null||p.topic.equalsIgnoreCase(topic)).filter(p->difficulty==null||p.difficulty==difficulty).toList();int from=Math.min(page*Math.min(size,100),filtered.size()),to=Math.min(from+Math.min(size,100),filtered.size());return Map.of("items",filtered.subList(from,to).stream().map(Api::problem).toList(),"page",page,"size",size,"total",filtered.size());
  }
  @GetMapping("/{id}") Map<String,Object> detail(@PathVariable UUID id){return problems.findById(id).map(Api::problem).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Problem not found"));}
}
