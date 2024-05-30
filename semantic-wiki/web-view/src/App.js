import React from 'react';
import CytoscapeComponent from 'react-cytoscapejs';
// import json from './jsons/graph.json';
import Cytoscape from 'cytoscape';
import { Container, Row, Col } from 'react-grid-system';
import klay from 'cytoscape-klay';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import { RingLoader } from 'react-spinners';
import Switch from '@mui/material/Switch';
import FormControlLabel from '@mui/material/FormControlLabel';


Cytoscape.use(klay)

class App extends React.Component {
  // data source for visualizing
  elements = [];

  // init method for react life cycle
  constructor(props) {
    super(props);

    // all event handler
    this.handleClick = this.handleClick.bind(this);
    this.handleChangeFrom = this.handleChangeFrom.bind(this);
    this.handleChangeTo = this.handleChangeTo.bind(this);
    this.handleChangeConstraint = this.handleChangeConstraint.bind(this);
    this.handleChangeCategory = this.handleChangeCategory.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);

    this.flag = true
    // flag for showing progress bar
    this.isLoading = false

    // react state variables
    this.state = {
      elements: [],
      showDiv: true,
      from: '',
      to: '',
      const: '',
      category: '',
      isChecked: false,
      height: 500,
      width: 1000,
      isLoading: false,
      indicationText: "",
      endBasedOnCategory: false,
      query: '',
      isQueryEnabled: false,
      isHidden: '',
    };
  }

  componentDidMount() {
    // get query params from url
    const queryParameters = new URLSearchParams(window.location.search)

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


    if (this.upperH == "true") {
      this.setState({category : this.category})
    }
    else if (this.sparql_search == "true") {
      this.setState({query: this.sparql})
    }

    // set infos into state
    this.setState({ from: this.from, to: this.to, const: this.constraint, height: this.cy_height, width: this.cy_width, category: this.category });
  }

  componentWillUpdate() {
    
  }

  componentDidUpdate() {
    // update request param when dom is changed
    let s = this.state

    if (this.upperH == "true") {
      console.log(this.upperH)
      document.getElementById('hierarchy-selector').click()
      this.upperH = false
      console.log(this.state.isChecked)
    }

    if (this.sparql_search == "true") {
      document.getElementById('query-search-selector').click()
      this.sparql_search = false
    }

    // set request parameters based on mood
    if (s.isQueryEnabled) {
      this.reqestParam = `query=${JSON.stringify(s.query)}`
    } else {
      this.reqestParam = `from=${s.from}&to=${s.to}&category=${s.category}&const=${s.const}&upper=${s.isChecked}`
    }

    // hint for mandatory field
    document.getElementById('mandatory-heading').style.color = "red";
    if (s.isChecked) {
      document.getElementById('end-material-category').style.color = "red";
      //document.getElementById('end-material-heading').style.color = "black";
    } else {
      //document.getElementById('end-material-heading').style.color = "red";
      document.getElementById('end-material-category').style.color = "black";
    }

    // update hidden information for query viewing
    if (s.isQueryEnabled) {
      document.getElementById('search-by-mat-selection').hidden = 'hidden'
      document.getElementById('search-by-sparql').hidden = ''
    } else {
      document.getElementById('search-by-mat-selection').hidden = ''
      document.getElementById('search-by-sparql').hidden = 'hidden'
    }
  }

  handleSwitchToggle = () => {
    // change state for switch
    this.flip = !this.state.isChecked
    this.setState({ isChecked: this.flip})

    // now API call from here for category view
  };

  handleQuerySwitchToggle = () => {
    // change state for switch
    this.flip = !this.state.isQueryEnabled
    this.setState({ isQueryEnabled: this.flip})

    // unset previous data
    this.setState({ elements: [] });
    // now API call from here for category view
  };

  toggleDiv = () => {
    this.setState({ showDiv: !this.state.showDiv });
  }

  progressBarHeight = () => {
    return this.state.isLoading ? this.state.height : 0;
  }

  handleChangeFrom(event) {
    this.setState({ from: event.target.value });
  }

  handleChangeTo(event) {
    this.setState({ to: event.target.value });
  }

  handleChangeConstraint = (event) => {
    this.setState({ const: event.target.value });
  }

  handleChangeCategory = (event) => {
    this.setState({ category: event.target.value });
  }

  handleChangeSPAQRL = (event) => {
    console.log(JSON.stringify(event.target.value))
    this.setState({ query: event.target.value });
  }

  // click handler when user click on any component in WIKI
  handleClick(event) {
    const node = event.target;
    if (node.isNode()) {
      let url = node.data('url');
      // replace localhost text with real IP
      const modUrl = url.replace("localhost", "hostpc_ip_address");
      if (modUrl) {
        window.open(modUrl, '_blank');
      }
    }
  }

  // submit handler on submit button press
  handleSubmit = async (event) => {
    let st = this.state
    
    // first check if we are searching by query or material serach
    if (st.isQueryEnabled) {
      // check is query string is empty or not
      if (!st.query) {
        window.alert("There is something wrong with Query String!")
        return
      }

      event.preventDefault();
      // loading start here
      this.setState({ isLoading: true });
      // API invocation
      let sparql = this.state.query
      const response = await fetch('http://hostpc_ip_address:8080/search-by-sparql', {
        method: 'POST',
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Content-Type': 'application/json',
        },
        body: sparql,//JSON.stringify({ sparql })
      });

      const data = await response.json();

      // check if there is any paths or not
      if (Object.keys(data.elements).length === 0) {
        this.setState({ isLoading: false });
        window.alert("There is no path between with this query")
      }
      // loading ends here
      this.setState({ isLoading: false });

      this.setState({ elements: data.elements });
      this.toggleDiv()

    } else {
      // check for material search
      // if isChecked 
      if (st.isChecked) {
        // now check if category is empty or not?
        if (!st.category){
          window.alert("Please select category!")
          return
        }
      } else {
        // First :: check if `to` is empty or not
        if (!this.state.isChecked && this.state.to == null) {
          window.alert("End Product couldn't be empty!")
          return
        }

        // Second :: check if `from` and `to` contains same value or not
        if (this.state.from === this.state.to) {
          window.alert("You select same Parts as From and To!")
          return
        }
      }

      event.preventDefault();
      // loading start here
      this.setState({ isLoading: true });
      // API invocation
      const response = await fetch('http://hostpc_ip_address:8080/all-paths?' + this.reqestParam, {
        method: 'POST',
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Content-Type': 'application/json',
        },
      });
      const data = await response.json();

      // check if there is any paths or not
      if (Object.keys(data.elements).length === 0) {
        this.setState({ isLoading: false });
        window.alert("There is no path between " + this.state.from + " and " + this.state.to)
      }
      // loading ends here
      this.setState({ isLoading: false });

      this.setState({ elements: data.elements });
      this.toggleDiv()
    }
  }


  render() {
    // layout definition
    const layout = {
      name: 'klay',
      nodeDimensionsIncludeLabels: true, // Include labels in node dimensions
      fit: true, // Whether to fit the viewport to the graph
      padding: 10, // Padding used on fit
      animate: false, // Whether to transition the node positions
      animateFilter: (node, i) => true, // Whether to animate specific nodes when animation is on; non-animated nodes immediately go to their final positions
      animationDuration: 500, // Duration of animation in ms if enabled
      animationEasing: undefined, // Easing of animation if enabled
    }

    return (
      <div>

        <Container>
          <form onSubmit={this.handleSubmit}>
            <Row align='center' direction='row-reverse'>
              <Col>
              <FormControlLabel
                  control={<Switch id='query-search-selector' checked={this.isQueryEnabled} onChange={this.handleQuerySwitchToggle} color="primary" />}
                  label={this.state.isQueryEnabled ? "Want to go back by Material Search?" : "Want to search by Query?"}
                />
              </Col>
            </Row>
            <div id='search-by-mat-selection'>
            <Row align='Center'> 
              <Col sm={3}> 
                <h3> All Paths of processes </h3> 
              </Col>

              <Col sm={7}>
                <FormControlLabel
                  control={<Switch id='hierarchy-selector' checked={this.isChecked} onChange={this.handleSwitchToggle} color="primary" />}
                  label={this.state.isChecked ? "You only need to select category" : "Select Upper Hierarchy?"}
                />
              </Col>
            </Row>
            
            <br />

            <Row align='center'>
              <Col><div id='mandatory-heading'><i>All red colored are mandatory.</i></div></Col>
            </Row>

            
            <Row align='center'>

              <Col sm={1}>
                <Box><h4>Start Component</h4></Box>
              </Col>
              <Col sm={1.5}>
                <Box>
                  <FormControl fullWidth>
                    <InputLabel id="demo-simple-select-label">Parts</InputLabel>
                    <Select
                      labelId="demo-simple-select-label"
                      id="demo-simple-select"
                      disabled={this.state.isChecked}
                      value={this.state.from}
                      onChange={this.handleChangeFrom}
                      label="Parts"
                    >
                      <MenuItem value="">Select One</MenuItem>
                      <MenuItem value="Elektroband">Elektroband</MenuItem>
                      <MenuItem value="Wellenmaterial">Wellenmaterial</MenuItem>
                      <MenuItem value="Stator">Stator</MenuItem>
                      <MenuItem value="Gehäuse">Gehäuse</MenuItem>
                      <MenuItem value="Rotor">Rotor</MenuItem>
                      <MenuItem value="Blechpaket">Blechpaket</MenuItem>
                      <MenuItem value="Gehäusematerial">Gehäusematerial</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
              </Col>
              
              <Col sm={1}>
                <Box><div id='end-material-heading'><h4>End Component</h4></div></Box>
              </Col>
              <Col sm={1.5}>
                <Box>
                  <FormControl fullWidth>
                    <InputLabel id="demo-simple-select-label">Parts</InputLabel>
                    <Select
                      labelId="demo-simple-select-label"
                      id="demo-simple-select"
                      disabled={this.state.isChecked}
                      value={this.state.to}
                      onChange={this.handleChangeTo}
                      label="Hairpins"
                    >
                      <MenuItem value="">Select One</MenuItem>
                      <MenuItem value="Hairpins">Hairpins</MenuItem>
                      <MenuItem value="Stator">Stator</MenuItem>
                      <MenuItem value="Welle">Welle</MenuItem>
                      <MenuItem value="Blechpaket">Blechpaket</MenuItem>
                      <MenuItem value="Gehäuse">Gehäuse</MenuItem>
                      <MenuItem value="Rotor">Rotor</MenuItem>
                      <MenuItem value="Elektromotor">Elektromotor</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
              </Col>

              <Col sm={1}>
                <Box><h4>Constraint</h4></Box>
              </Col>
              <Col sm={1.7}>
                <Box>
                  <FormControl fullWidth>
                    <InputLabel id="demo-simple-select-label2">Constraint</InputLabel>
                    <Select
                      labelId="demo-simple-select-label2"
                      id="demo-simple-select2"
                      disabled={this.state.isChecked}
                      value={this.state.const}
                      onChange={this.handleChangeConstraint}
                      label="Mass Production"
                    >
                      <MenuItem value="">Select One</MenuItem>
                      <MenuItem value="Hat_Klein-2D-2FMittelserie">Mass Production</MenuItem>

                    </Select>
                  </FormControl>
                </Box>
              </Col>

              <Col sm={1}>
                <Box><div id='end-material-category'><h4>Category</h4></div></Box>
              </Col>
              <Col sm={1.5}>
                <Box>
                  <FormControl fullWidth>
                    <InputLabel id="demo-simple-select-label3">Category</InputLabel>
                    <Select
                      labelId="demo-simple-select-label3"
                      id="demo-simple-select3"
                      value={this.state.category}
                      onChange={this.handleChangeCategory}
                      label="Mass Production"
                    >
                      <MenuItem value="">Select One</MenuItem>
                      <MenuItem value="Hairpinstatorproduction">Hairpinstatorproduktion</MenuItem>
                      <MenuItem value="Blechpaketfertigung">Blechpaketfertigung</MenuItem>
                      <MenuItem value="Wellenfertigung">Wellenfertigung</MenuItem>
                      <MenuItem value="Gehäusefertigung">Gehäusefertigung</MenuItem>
                      {/*
                      <MenuItem value="konventionelleStatorproduktion">konventionelle Statorproduktion</MenuItem>
                      <MenuItem value="RotorfertigungFSM">Rotorfertigung FSM</MenuItem>
                      <MenuItem value="RotorfertigungPSM">Rotorfertigung PSM</MenuItem>
                      <MenuItem value="RotorfertigungASM">Rotorfertigung ASM</MenuItem>
                      */}
                      
                    </Select>
                  </FormControl>
                </Box>
              </Col>
              <Col sm={1}>
                <Button id='search-btn' variant="contained" type="submit">Search</Button>
              </Col>
              <br />
            </Row>
            </div>

            {/* For direct query input */}
            <div id='search-by-sparql'>
            <br />
              <Row align='center' justify='center'><h3>Enter SPARQL in TextField</h3></Row>
              <Row align='center'>
                {/* first column is for textfield */}
                <Col width={1000}>
                <textarea
                  rows={10}
                  cols={100}
                  value={this.state.query}
                  onChange={this.handleChangeSPAQRL}
                  margin='dense'            
                />
                {/*
                <TextField multiline='true' 
                  label='Enter SPARQL' 
                  fullWidth='true'
                  onChange={this.handleChangeSPAQRL}
                  value={this.state.query}>

                </TextField>
                */}
                </Col>
                {/* second column is for submit */}
                <Col>
                <Button id='query-btn' variant="contained" type="submit">Show</Button>
                </Col>
              </Row>
            </div>
          </form>

          <br />
          <br />

          
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: `${this.state.isLoading?700:0}px`, width:'100%', disabled: this.state.isLoading }}>
            <RingLoader size={100} color="#258128" loading={this.state.isLoading} />
          </div>
          
          <CytoscapeComponent

          layout={layout}
          elements={this.state.elements}
          cy={Cytoscape}
          style={{ width: "100%", height: `${this.state.height}px` }}
          stylesheet={[
            {
              selector: 'node[type="process"]',
              style: {
                shape: 'polygon',
                'shape-polygon-points': '.75, 0,  0.5, 1,  -1, 1,  -0.75, 0,  -1, -1,  0.5, -1',
                height: 30,
                width: 100,
                'background-color': '#97C139',
                label: 'data(label)',

                'text-valign': 'center',
                'text-halign': 'center',
                'font-size': 5
              }
            },
            {
              selector: 'node[type="component"]',
              style: {
                shape: 'hexagon',
                'background-color': '#BFBFBF',
                label: 'data(label)',
                height: 30,
                width: 100,
                'text-valign': 'center',
                'text-halign': 'center',
                'font-size': 5
              }
            },
            {
              selector: 'edge',
              style: {
                width: 1,
                label: 'data(label)',
                'curve-style': 'bezier',
                'target-arrow-shape': 'triangle',
                'line-color': '#ccc',
                'target-arrow-color': '#ccc',
                "font-size": 4,

              }
            },


          ]}


          cy={(cy) => {
            cy.on('tap', 'node', this.handleClick);
          }}
        />

        </Container>
      </div>
    );
  }
}

export default App;