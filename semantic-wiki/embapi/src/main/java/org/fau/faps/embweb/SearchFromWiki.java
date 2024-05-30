package org.fau.faps.embweb;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class SearchFromWiki {

	// API of search from all possible material to specific material and also consider constrains or not!  
	@PostMapping("/all-paths")
	@CrossOrigin(origins = "*")
    public ResponseEntity<Object> searchToFromAllPossibles(@RequestParam(value = "from", required = false) String startMaterial, 
    		@RequestParam(value = "to", required = false) String endMaterial, @RequestParam(value="const", required = false) String constraint,
    		@RequestParam(value = "category", required = false, defaultValue = "") String category,
    		@RequestParam(value = "upper", required = true, defaultValue = "false") boolean isUpperH)
	{
		
		System.out.println("------------------------------------------------------------------------------------------");
		System.out.println("Start:" + startMaterial + "End:" + endMaterial + "Constraint:" + constraint + "Categoty:" + category);
		
		// check if all parameters are invalid then return error message
		if(!ServiceUtils.isValid(startMaterial) && !ServiceUtils.isValid(endMaterial) && !ServiceUtils.isValid(constraint) 
			&& !ServiceUtils.isValid(category)){
			return ResponseEntity.ok()
			        .contentType(MediaType.APPLICATION_JSON)
			        .body("All Parameters are Empty!");
		}
		
		SearchService ss = new SearchService();
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = ss.getAllPathsFrom(startMaterial, endMaterial, constraint, category, isUpperH);
		
		if(jsonStr == null) {
			return ResponseEntity.internalServerError().build();
		}
		
		try {
		      // Convert JSON string to JSON object
		      Object json = mapper.readValue(jsonStr, Object.class);

		      // Return response with JSON object as the body
		      return ResponseEntity.ok()
		        .contentType(MediaType.APPLICATION_JSON)
		        .body(json);
		    } catch (JsonProcessingException e) {
		      e.printStackTrace();
		      return ResponseEntity.badRequest().build();
		    }
    }
	
	// API of search from all possible material to specific material and also consider constrains or not!  
	@PostMapping("/search-by-sparql")
	@CrossOrigin(origins = "*")
    public ResponseEntity<Object> searchBySPARQL(
    		@RequestBody String query)
	{
		
		System.out.println("------------------------------------------------------------------------------------------");
		System.out.println("Query:" + query);
		
		// check if all parameters are invalid then return error message
		if(!ServiceUtils.isValid(query)){
			return ResponseEntity.ok()
			        .contentType(MediaType.APPLICATION_JSON)
			        .body("All Parameters are Empty!");
		}
		
		SearchService ss = new SearchService();
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = ss.getAllPathsFrom(query);
		
		if(jsonStr == null) {
			return ResponseEntity.internalServerError().build();
		}
		
		try {
		      // Convert JSON string to JSON object
		      Object json = mapper.readValue(jsonStr, Object.class);

		      // Return response with JSON object as the body
		      return ResponseEntity.ok()
		        .contentType(MediaType.APPLICATION_JSON)
		        .body(json);
		    } catch (JsonProcessingException e) {
		      e.printStackTrace();
		      return ResponseEntity.badRequest().build();
		    }
    }
	
	// Entry end point for embweb api
	@GetMapping("/")
	public String index() {
		return "Welcome to EMB Wiki API index!";
	}
	
}
