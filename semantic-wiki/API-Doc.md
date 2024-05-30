1. Connecting webapp through API.
   The web app is running on port 3000 on the host PC (192.168.209.140). The only way to provide information and obtain a view is by adding data to the request parameters.
   All keywords those are important to mention are listed below.
   * to activate search by sparql, need to add ['query-search' to 'false/true', ex: 192.168.209.140:3000/?query-search=true]
   * to activate upper hierarchy searching, need to add ['upper-hierarchy' to 'false/true', ex: 192.168.209.140:3000/?upper-hierarchy=true]
   * to add sparql query, need to add ['query' to encoded string, ex: 192.168.209.140:3000/?query={string_text}]
   use this codeblock as reference
   ```
    this.sparql_search = queryParameters.get('query-search')
    this.sparql = queryParameters.get("query")
  
    this.upperH = queryParameters.get('upper-hierarchy')
  
    // url parameters
    this.from = queryParameters.get("from")
    this.to = queryParameters.get("to")
    this.constraint = queryParameters.get("const")
    this.cy_height = queryParameters.get("height")
    this.cy_width = queryParameters.get("width")
    this.category = queryParameters.get("category")
   ```
   
