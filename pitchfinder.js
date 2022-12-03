/*
The MIT License (MIT)

Copyright (c) 2014 Chris Wilson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
  
var CHROMATIC = [ 'C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B' ];
var header = [ "Scale","pa","dha","ni","sa","re","ga","ma","pa","dha","ni","sa","re","ga","ma","Count"];
var mid_csharp = ["2G#","2A#","2C","3C#","3D#","3F","3F#","3G#","3A#","3C","4C#","4D#","4F","4F#"];
var mid_D = ["2A","2B","2C#","3D","3E","3F#","3G","3A","3B","3C#","4D","4E","4F#","4G"];
var mid_Dsharp = ["2A#","2C","2D","3D#","3F","3G","3G#","3A#","3C","3D","4D#","4F","4G","4G#"];
var mid_E = ["2B","2C#","2D#","3E","3F#","3G#","3A","3B","3C#","3D#","4E","4F#","4G#","4A"];
var mid_F = ["2C","2D","2E","3F","3G","3A","3A#","3C","3D","3E","4F","4G","4A","4A#"];
var mid_Fsharp = ["2C#","2D#","2F","3F#","3G#","3A#","3B","3C#","3D#","3F","4F#","4G#","4A#","4B"];
var mid_G = ["2D","2E","2F#","3G","3A","3B","3C","3D","3E","3F#","4G","4A","4B","4C"];
var mid_Gsharp = ["2D#","2F","2G","3G#","3A#","3C","3C#","3D#","3F","3G","4G#","4A#","4C","4C#"];
var mid_A = ["2E","2F#","2G#","3A","3B","3C#","3D","3E","3F#","3G#","4A","4B","4C#","4D"];
var mid_Asharp = ["2F","2G","2A","3A#","3C","3D","3D#","3F","3G","3A","4A#","4C","4D","4D#"];
var mid_B = ["2F#","2G#","2A#","3B","3C#","3D#","3E","3F#","3G#","3A#","4B","4C#","4D#","4E"];
var mid_C = ["2G","2A","2B","3C","3D","3E","3F","3G","3A","3B","4C","4D","4E","4F"];
var base_Csharp = ["3G#","3A#","3C","4C#","4D#","4F","4F#","4G#","4A#","4C","5C#","5D#","5F","5F#"];
var base_D = ["3A","3B","3C#","4D","4E","4F#","4G","4A","4B","4C#","5D","5E","5F#","5G"];
var base_Dsharp = ["3A#","3C","3D","4D#","4F","4G","4G#","4A#","4C","4D","5D#","5F","5G","5G#"];
var base_E = ["3B","3C#","3D#","4E","4F#","4G#","4A","4B","4C#","4D#","5E","5F#","5G#","5A"];
var base_F = ["3C","3D","3E","4F","4G","4A","4A#","4C","4D","4E","5F","5G","5A","5A#"];
var base_Fsharp = ["3C#","3D#","3F","4F#","4G#","4A#","4B","4C#","4D#","4F","5F#","5G#","5A#","5B"];
var base_G = ["3D","3E","3F#","4G","4A","4B","4C","4D","4E","4F#","5G","5A","5B","5C"];
var base_Gsharp = ["3D#","3F","3G","4G#","4A#","4C","4C#","4D#","4F","4G","5G#","5A#","5C","5C#"];
var base_A = ["3E","3F#","3G#","4A","4B","4C#","4D","4E","4F#","4G#","5A","5B","5C#","5D"];
var base_Asharp = ["3F","3G","3A","4A#","4C","4D","4D#","4F","4G","4A","5A#","5C","5D","5D#"];
var base_B = ["3F#","3G#","3A#","4B","4C#","4D#","4E","4F#","4G#","4A#","5B","5C#","5D#","5E"];
var base_C = ["3G","3A","3B","4C","4D","4E","4F","4G","4A","4B","5C","5D","5E","5F"];

// var flutes = [];
var flutes = [mid_csharp,
              mid_D ,
              mid_Dsharp ,
              mid_E ,
              mid_F ,
              mid_Fsharp ,
              mid_G ,
              mid_Gsharp ,
              mid_A ,
              mid_Asharp ,
              mid_B ,
              mid_C ,
              base_Csharp,
              base_D ,
              base_Dsharp ,
              base_E ,
              base_Fsharp ,
              base_G ,
              base_Gsharp ,
              base_A ,
              base_Asharp ,
              base_B ,
              base_C
              ];


window.AudioContext = window.AudioContext || window.webkitAudioContext;

var audioContext = null;
var isPlaying = false;
var sourceNode = null;
var analyser = null;
var theBuffer = null;
var DEBUGCANVAS = null;
var mediaStreamSource = null;
var detectorElem, 
	canvasElem,
	waveCanvas,
	pitchElem,
	noteElem,
	detuneElem,
	detuneAmount;

window.onload = function() {
	audioContext = new AudioContext();
	MAX_SIZE = Math.max(4,Math.floor(audioContext.sampleRate/5000));	// corresponds to a 5kHz signal

	detectorElem = document.getElementById( "detector" );
	canvasElem = document.getElementById( "output" );
	DEBUGCANVAS = document.getElementById( "waveform" );
	if (DEBUGCANVAS) {
		waveCanvas = DEBUGCANVAS.getContext("2d");
		waveCanvas.strokeStyle = "black";
		waveCanvas.lineWidth = 1;
	}
	pitchElem = document.getElementById( "pitch" );
	noteElem = document.getElementById( "note" );
	detuneElem = document.getElementById( "detune" );
	detuneAmount = document.getElementById( "detune_amt" );

	detectorElem.ondragenter = function () { 
		this.classList.add("droptarget"); 
		return false; };
	detectorElem.ondragleave = function () { this.classList.remove("droptarget"); return false; };
	detectorElem.ondrop = function (e) {
  		this.classList.remove("droptarget");
  		e.preventDefault();
		theBuffer = null;

	  	var reader = new FileReader();
	  	reader.onload = function (event) {
	  		audioContext.decodeAudioData( event.target.result, function(buffer) {
	    		theBuffer = buffer;
	  		}, function(){alert("error loading!");} ); 

	  	};
	  	reader.onerror = function (event) {
	  		alert("Error: " + reader.error );
		};
	  	reader.readAsArrayBuffer(e.dataTransfer.files[0]);
	  	return false;
	};
	
	fetch('whistling3.ogg')
		.then((response) => {
			if (!response.ok) {
				throw new Error(`HTTP error, status = ${response.status}`);
			}
			return response.arrayBuffer();
		}).then((buffer) => audioContext.decodeAudioData(buffer)).then((decodedData) => {
			theBuffer = decodedData;
		});

}

function startPitchDetect() {	
    // grab an audio context
    audioContext = new AudioContext();

    // Attempt to get audio input
    navigator.mediaDevices.getUserMedia(
    {
        "audio": {
            "mandatory": {
                "googEchoCancellation": "false",
                "googAutoGainControl": "false",
                "googNoiseSuppression": "false",
                "googHighpassFilter": "false"
            },
            "optional": []
        },
    }).then((stream) => {
        // Create an AudioNode from the stream.
        mediaStreamSource = audioContext.createMediaStreamSource(stream);

	    // Connect it to the destination.
	    analyser = audioContext.createAnalyser();
	    analyser.fftSize = 2048;
	    mediaStreamSource.connect( analyser );
	    updatePitch();
    }).catch((err) => {
        // always check for errors at the end.
        console.error(`${err.name}: ${err.message}`);
        alert('Stream generation failed.');
    });
}


function toggleLiveInput() {
    if (isPlaying) {
        //stop playing and return
        sourceNode.stop( 0 );
        sourceNode = null;
        analyser = null;
        isPlaying = false;
		if (!window.cancelAnimationFrame)
			window.cancelAnimationFrame = window.webkitCancelAnimationFrame;
        window.cancelAnimationFrame( rafID );
    }
    getUserMedia(
    	{
            "audio": {
                "mandatory": {
                    "googEchoCancellation": "false",
                    "googAutoGainControl": "false",
                    "googNoiseSuppression": "false",
                    "googHighpassFilter": "false"
                },
                "optional": []
            },
        }, gotStream);
}


var rafID = null;
var tracks = null;
var buflen = 2048;
var buf = new Float32Array( buflen );
var noteStrings = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];

function noteFromPitch( frequency ) {
	var noteNum = 12 * (Math.log( frequency / 440 )/Math.log(2) );
	return Math.round( noteNum ) + 69;
}

function frequencyFromNoteNumber( note ) {
	return 440 * Math.pow(2,(note-69)/12);
}

function centsOffFromPitch( frequency, note ) {
	return Math.floor( 1200 * Math.log( frequency / frequencyFromNoteNumber( note ))/Math.log(2) );
}

function autoCorrelate( buf, sampleRate ) {
	// Implements the ACF2+ algorithm
	var SIZE = buf.length;
	var rms = 0;

	for (var i=0;i<SIZE;i++) {
		var val = buf[i];
		rms += val*val;
	}
	rms = Math.sqrt(rms/SIZE);
	if (rms<0.01) // not enough signal
		return -1;

	var r1=0, r2=SIZE-1, thres=0.2;
	for (var i=0; i<SIZE/2; i++)
		if (Math.abs(buf[i])<thres) { r1=i; break; }
	for (var i=1; i<SIZE/2; i++)
		if (Math.abs(buf[SIZE-i])<thres) { r2=SIZE-i; break; }

	buf = buf.slice(r1,r2);
	SIZE = buf.length;

	var c = new Array(SIZE).fill(0);
	for (var i=0; i<SIZE; i++)
		for (var j=0; j<SIZE-i; j++)
			c[i] = c[i] + buf[j]*buf[j+i];

	var d=0; while (c[d]>c[d+1]) d++;
	var maxval=-1, maxpos=-1;
	for (var i=d; i<SIZE; i++) {
		if (c[i] > maxval) {
			maxval = c[i];
			maxpos = i;
		}
	}
	var T0 = maxpos;

	var x1=c[T0-1], x2=c[T0], x3=c[T0+1];
	a = (x1 + x3 - 2*x2)/2;
	b = (x3 - x1)/2;
	if (a) T0 = T0 - b/(2*a);

	return sampleRate/T0;
}

function updatePitch( time ) {
	var cycles = new Array;
	analyser.getFloatTimeDomainData( buf );
	var ac = autoCorrelate( buf, audioContext.sampleRate );
	// TODO: Paint confidence meter on canvasElem here.

	if (DEBUGCANVAS) {  // This draws the current waveform, useful for debugging
		waveCanvas.clearRect(0,0,512,256);
		waveCanvas.strokeStyle = "red";
		waveCanvas.beginPath();
		waveCanvas.moveTo(0,0);
		waveCanvas.lineTo(0,256);
		waveCanvas.moveTo(128,0);
		waveCanvas.lineTo(128,256);
		waveCanvas.moveTo(256,0);
		waveCanvas.lineTo(256,256);
		waveCanvas.moveTo(384,0);
		waveCanvas.lineTo(384,256);
		waveCanvas.moveTo(512,0);
		waveCanvas.lineTo(512,256);
		waveCanvas.stroke();
		waveCanvas.strokeStyle = "black";
		waveCanvas.beginPath();
		waveCanvas.moveTo(0,buf[0]);
		for (var i=1;i<512;i++) {
			waveCanvas.lineTo(i,128+(buf[i]*128));
		}
		waveCanvas.stroke();
	}

 	if (ac == -1) {
 		detectorElem.className = "vague";
	 	pitchElem.innerText = "--";
		noteElem.innerText = "-";
		detuneElem.className = "";
		detuneAmount.innerText = "--";
 	} else {
	 	detectorElem.className = "confident";
	 	pitch = ac;
	 	pitchElem.innerText = Math.round( pitch ) ;
	 	var note =  noteFromPitch( pitch );
        var oct = Math.floor(note / 12) - 2
        var notedisp = oct + noteStrings[note%12];
        bufferNotes.push(notedisp);
        if(bufferNotes.length > 10){
            bufferNotes.shift();
        }
        if(checkOccurrence(bufferNotes,notedisp) > 9){
            if(!notesArr.includes(notedisp) && oct > 2 && oct < 6){
                notesArr.push(notedisp);
            }    
            findFlute(notesArr,notedisp);    
            document.getElementById("notes-display").innerHTML = notesArr;
        }
        // noteElem.innerHTML = oct + "-" + (note%12) ;
		noteElem.innerHTML = notedisp;
		var detune = centsOffFromPitch( pitch, note );
		if (detune == 0 ) {
			detuneElem.className = "";
			detuneAmount.innerHTML = "--";
		} else {
			if (detune < 0)
				detuneElem.className = "flat";
			else
				detuneElem.className = "sharp";
			detuneAmount.innerHTML = Math.abs( detune );
		}
	}

	if (!window.requestAnimationFrame)
		window.requestAnimationFrame = window.webkitRequestAnimationFrame;
	rafID = window.requestAnimationFrame( updatePitch );
}

let notesArr = [];
let bufferNotes = [];
var flutelist = document.querySelector('#flutes ul');

function getHeader(){
    var headerRow = "<tr>"
    for(let k = 0; k < header.length; k++) {
      headerRow += "<td>" + header[k] + "</td>";
    }
    headerRow += "</tr>"
    return headerRow
  }
  
  function findNoteAllOctaves(currentNote,scaleNotes){      
    var tdNote = ""
    if (!scaleNotes.includes(currentNote)){        
      var nfBgColor = "white";
      for(let k = 0; k < scaleNotes.length; k++) {        
        if(scaleNotes[k].substring(1) == currentNote.substring(1)){
          nfBgColor = "#90EE90"
          break;
        }
      }
      tdNote = "<td bgColor='" + nfBgColor + "'>" +  currentNote + "</td>";
    }
    return tdNote;
  }
  
  function findFlute(currentNotes, notedsip) { 
   currentNotes.sort();
   flutelist.innerHTML ="";
   var byCount = [];
   var mytable = "<table>"; 
   mytable += getHeader();
   for(let i = 0; i < flutes.length; i++) {
      var flute = flutes[i];
      var count = 0;
      var notFoundCount = 0; 
      var rowStr = "<tr>";
      rowStr += "<td bgcolor='yellow'>" + flute[3] + "</td>"
      var notFound = "<td bgcolor='red'> NF </td>"
      for(let j = 0; j < flute.length; j++) {
          var bgcolor = "white";
          for(let k = 0; k < currentNotes.length; k++) {
              if (currentNotes[k] == flute[j]){
                  count = count + 1;
                  bgcolor = "#90EE90"
              }
              if (flute[j] == notedsip){
                bgcolor = "green"
              }
          }
          rowStr += "<td bgcolor='"+ bgcolor  + "'>" + flute[j] + "</td>"
      }
      for(let k = 0; k < currentNotes.length; k++) {
          // if (!flute.includes(currentNotes[k])){
          //   notFound += "<td>" + currentNotes[k] +"</td>"
          // }
          var nfTdStr = findNoteAllOctaves(currentNotes[k],flute)
          if(nfTdStr){
            notFoundCount++;
            notFound += nfTdStr;
          }
      }
      rowStr += "<td bgcolor='yellow'>" + count + "</td>"
      for(let m = 0 ; m < 12 - notFoundCount; m++){
        notFound += "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>"
      }
      rowStr += notFound;
      rowStr += "</tr>";
      countArry = [count, rowStr]
      byCount.push(countArry)
      // newItem.appendChild(document.createTextNode(flutes[i] + "[" + count + "]"));
      // flutelist.appendChild(newItem);        
   }   
   var sortedData = byCount.sort((a, b) => b[0] - a[0])
   for(let k = 0; k < sortedData.length; k++) {
     var data = sortedData[k];
     mytable += data[1] ;
   }        
   mytable += "</table>";
   document.getElementById("flutes").innerHTML = mytable;
  }

  document.addEventListener('keydown', (event) => {
    var name = event.key;
    var code = event.code;
    // Alert the key name and key code on keydown
    if (code == "Space"){      
        document.getElementById("flutes").innerHTML = "";
        document.getElementById("notes-display").innerHTML = ""
        notesArr = [];        
        startPitchDetect();
        toggleLiveInput();        
        // alert(`Key pressed ${name} \r\n Key code value: ${code}`);
    }
  }, false);

  const checkOccurrence = (array, element) => {
    let counter = 0;
    for (item of array.flat()) {
        if (item == element) {
            counter++;
        }
    };
    return counter;
};

  