
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

var midi, data;
var dataList = document.querySelector('#midi-data ul');
let notesArr = [];
var flutelist = document.querySelector('#flutes ul');

// start talking to MIDI controller
if (navigator.requestMIDIAccess) {
  navigator.requestMIDIAccess({
    sysex: false
  }).
    then(onMIDISuccess, onMIDIFailure);
} else {
  console.warn("No MIDI support in your browser");
}

// on success
function onMIDISuccess(midiData) {
  // this is all our MIDI data
  midi = midiData;
  var allInputs = midi.inputs.values();
  // loop over all available inputs and listen for any MIDI input
  for (var input = allInputs.next(); input && !input.done; input = allInputs.next()) {
    if (window.CP.shouldStopExecution(0)) break;
    // when a MIDI value is received call the onMIDIMessage function
    input.value.onmidimessage = gotMIDImessage;
  } window.CP.exitedLoop(0);
}

function gotMIDImessage(messageData) {
  var newItem = document.createElement('li');
  if (messageData.data[0] == 144) {        
    var note = getNote(messageData.data[1]);
    if (note == "-1C"){
      document.getElementById("flutes").innerHTML = "";
      document.getElementById("notes-display").innerHTML = ""
      notesArr = [];
    }else{
      newItem.appendChild(document.createTextNode(messageData.data + "<" + note + ">"));
      if(!notesArr.includes(note)){
        notesArr.push(note);
      }
      findFlute(notesArr,note);
      dataList.appendChild(newItem);
      document.getElementById("notes-display").innerHTML = notesArr.sort();        
    }
  }
}

// on failure
function onMIDIFailure() {
  console.warn("Not recognising MIDI controller");
}

function getNote(midi){
  if (isNaN(midi) || midi < 0 || midi > 127) return null
  var name = CHROMATIC[midi % 12]
  var oct = Math.floor(midi / 12) - 1
  return oct + name
}

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

function findFlute(currentNotes,note) { 
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
                bgcolor = "#90EE90";
            }
            if(flute[j] == note){
                bgcolor = "green";
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


