# SplunkMovieRestAssured
Splunk SDT Movie API
Download the Project or Acces via Git URL to run the SplunkMovieRestAssured Project - 
Developed Using- 
POM for JAVA Maven- IntellIJ - Rest Assured to test REST API Services


Discovered Bugs :

  
SPL 001: No two movies should have the same image : 3 movies have same images for "id": 186579, "id": 93560, poster paths are same and for “id": 138757.

SPL 002: All poster_path links must be valid. poster_path link of null is also acceptable 4 movies do not have poster_paths.

SPL 003: Sorting requirement. Part 1 Movies with genre_ids == null should be first in response. Part 2, if multiple movies have genre_ids == null, then sort by id (ascending). For movies that have non-null genre_ids, results should be sorted by id (ascending) Not sorted as per BR SPL 003 where Movies with genre_ids == null should be first in response.

SPL 004, SPL 005, SPL 006 have all passed and meets the given BR's

Note: Given batman is the queue id : For "id": 49026, "id": 20776, "id": 155, neither title or original_title includes “batman”
    
