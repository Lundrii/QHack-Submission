import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';

import axios from 'axios';
import base64 from 'base64-img';

const baseURL = "http://localhost:3000/"

const endpoint = "http://localhost:3000/upload"
const computeEnd = "http://localhost:3000/compute/"

const test = baseURL + "compute?min_dist=1&min_width=1&voltage=1&current=1"

class App extends Component {
  constructor(props) {
    super(props)
    this.state = {}
  }


  handleChange = name => event => {
    this.setState({
      [name]: event.target.value,
    });
    
  };

  handleUpload = () => {
    const data = new FormData()
    data.append('gerber', this.state.selectedFile, this.state.selectedFile.name)

    axios
      .post(endpoint, data, {
        onUploadProgress: ProgressEvent => {
          this.setState({
            loaded: (ProgressEvent.loaded / ProgressEvent.total * 100),
          })
        },
      })
      .then(res => {
        console.log(res.statusText);
        if (res.statusText == 'OK') {
          this.setState({displayImage : true});
        }
      });

      //const query = baseURL + "compute?min_dist=" + this.state.min_dist + "&min_width=" + this.state.min_width + "&voltage=" + this.state.voltage + "&current=" + this.state.current;
  }

  handleCompute = () => {
    console.log("Compute clicked")
    console.log(this.state);
    axios.post("http://localhost:3000/compute/", {
      min_dist: this.state.min_dist,
      min_width: this.state.min_width,
      voltage: this.state.voltage,
      current: this.state.current
    }).then(response => {
      console.log(response);
      this.setState({
        viableCurrent: response.data.currentRating,
        data: response.data
      });
    }).catch(function (err) {
      console.log(err);
    });
  }

  handleClick = () => {
    console.log("Button clicked")
  }

  handleselectedFile = event => {
    this.setState({
      selectedFile: event.target.files[0],
      loaded: 0,
    })
    console.log(event.target.files[0]);
  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          {/*<img src={logo} className="App-logo" alt="logo" />*/}
          {/*<p>
            Edit <code>src/App.js</code> and save to reload.
          </p>*/}
          <p>Welcome to <code>CircuitSafe</code>✔</p>
        </header>
        <div>
          <p>Measurements in mil (thousandth of an inch)</p>
          <p>Voltage in Volts and Current is given in Amperes</p>
        </div>
        <div>
          <TextField
            id="min_width"
            label="Minimum PCB Trace Width"
            type="number"
            InputLabelProps={{
              shrink: true,
            }}
            onChange={this.handleChange('min_width')}
            margin="normal"
          />
          <TextField
            id="min_dist"
            label="Minimum Distance Between Traces"
            type="number"
            InputLabelProps={{
              shrink: true,
            }}
            onChange={this.handleChange('min_dist')}
            margin="normal"
          />
          <TextField
            id="voltage"
            label="Circuit Voltage Specification"
            type="number"
            InputLabelProps={{
              shrink: true,
            }}
            onChange={this.handleChange('voltage')}
            margin="normal"
          />
          <TextField
            id="current"
            label="Circuit Amperage (A)"
            type="number"
            InputLabelProps={{
              shrink: true,
            }}
            onChange={this.handleChange('current')}
            margin="normal"
          />
        </div>
        <div>
          <input type="file" onChange={this.handleselectedFile} />
        </div>
        <div>
          <p></p>
          <Button color='primary' onClick={this.handleUpload}>Render PCB</Button>
        </div>
        <div>
          <Button color='primary' onClick={this.handleCompute}>Compute Values</Button>
        </div>
        <div>
          {
            this.state.viableCurrent ?
            <div>
              <p>Viable Current: {this.state.data.viableCurrent} A</p>
              <p>Electric Field: {this.state.data.electricField} N/C</p>
              <p>Signal Velocity: {this.state.data.signalSpeed} m/s</p>
              <p>Propagation Delay: {this.state.data.propDelay} S</p>
            </div>
            : null
            }
        </div>
        {
          this.state.displayImage ? 
          <div>
            <img src="http://localhost:3000/output.png"/>
          </div>
        : null
        }
      </div>
    );
  }
}

export default App;
