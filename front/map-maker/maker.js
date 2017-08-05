const line = "line";
const mine = "mine";
const remove = "remove";
let mode = line;

let select = 0;
let id = 2;
let eles = [];     // cytograph 操作データ
let changes = [];  // 内部用の操作データ
let json = {
  "sites": [{"id":0, "x":0.0, "y":0.0}, {"id":1, "x":8.03, "y":0.0}],
  "rivers": [{"source":0, "target":1}],
  "mines": [],
}

function initParams() {
  for (site of json.sites) {
    id = Math.max(id, site.id) + 1;
  }
  select = 0;
  eles = [];
  changes = [];
}

function renderGraph() {
  initParams();
  if (cy.elements !== undefined) {
    cy.destroy();
  }

  initCy(json, function () {
    cy.on('tap', function(event){
      var evtTarget = event.target;

      if(evtTarget === cy){
        let position = {
            x: event.originalEvent.offsetX,
            y: event.originalEvent.offsetY,
        };
        clickBackground(position);
      } else if (evtTarget.isNode()) {
        clickNode(evtTarget, event.originalEvent.shiftKey);
      } else if (evtTarget.isEdge()) {
        clickEdge(evtTarget);
      }
    });
  });
}

function clickBackground(position) {
  if (mode == line) {
    eles.push({doing:"add", addNode:true, source:select, ele:cy.add([
      {group:"nodes", data: {"id": id.toString()}, renderedPosition: {x: position.x, y: position.y}},
      {group:"edges", data: {source: select, target: id}}
    ])});
    select = id;
    id++;
  }
}

function clickNode(nodeMeta, shiftKey) {
  nodeId = nodeMeta.id();
  if (mode == line) {
    pre_select = select;
    select = nodeId;
    // shiftKeyが押されている場合は、selectが変わるだけ
    if (!shiftKey) {
      eles.push({doing:"add", addNode:false, source:pre_select, ele:cy.add([
        {group: "edges", data: {source:pre_select, target:select}}
      ])});
    }
  } else if (mode == mine) {
    if (!cy.$id(nodeId).hasClass(mine)) {
      eles.push({doing:"add", ele:cy.$id(nodeId).addClass(mine)});
    } else {
      eles.push({doing:"remove", ele:cy.$id(nodeId).removeClass(mine)});
    }
  } else if (mode == remove) {
    if (nodeId > 0) {
      eles.push({doing:"remove", ele:cy.remove(nodeMeta)});
      select = 0;
    }
  }
}

function undoCanvas() {
  if (eles.length > 0) {
    let ele = eles.pop();
    if (ele.doing == "add") {
      cy.remove(ele.ele);

      if (ele.source != undefined) {
        select = ele.source;
        if (ele.addNode) {
          id--;
        }
      }
    } else if (ele.doing == "remove") {
      cy.add(ele.ele);
    }
  }
}

function clickEdge(edgeMeta) {
  if (mode == remove) {
    eles.push({doing:"remove", ele:cy.remove(edgeMeta)});
  }
}

function resetCanvas() {
  cy.destroy();
  renderGraph();
}

function lineInputMode() {
  mode = line;
  $("#line-input").attr("disabled", true);
  $("#mine-input").removeAttr("disabled");
  $("#remove").removeAttr("disabled", true);
}

function mineInputMode() {
  mode = mine;
  $("#line-input").removeAttr("disabled");
  $("#mine-input").attr("disabled", true);
  $("#remove").removeAttr("disabled", true);
}

function removeMode() {
  mode = remove;
  $("#line-input").removeAttr("disabled");
  $("#mine-input").removeAttr("disabled", true);
  $("#remove").attr("disabled", true);
}

function round(number, decimalPlace) {
  if (decimalPlace == undefined) decimalPlace = 1;
  number *= Math.pow(10, decimalPlace - 1);
  number = Math.round(number);
  number /= Math.pow(10, decimalPlace - 1);
  return number;
}

function makeJsonFormat() {
  json = {"sites":[], "rivers":[], "mines":[]};
  cy_json = cy.json();

  for (let node of cy_json.elements.nodes) {
    json.sites.push({id:node.data.id, x:round(node.position.x/100, 4), y:round(node.position.y/100, 4)});
    if (cy.$id(node.data.id).hasClass(mine)) {
      json.mines.push(node.data.id);
    }
  }
  for (let edge of cy_json.elements.edges) {
    json.rivers.push({source:edge.data.source, target:edge.data.target});
  }

  if ($("input[name=format]:checked").val() == "true") {
    return JSON.stringify(json, null, "\t");
  } else { 
    return JSON.stringify(json);
  }
}

function processOutput() {
  let id = "#json-place";
  $(id).val(makeJsonFormat());
  console.log("Output as Json");
}

function doVisualize() {
  json = JSON.parse($("#json-form").val());
  renderGraph();
}

function keyPress(e) {
  var evtobj = window.event? event : e;
  if ((evtobj.keyCode == 90 || evtobj.keyCode == 91) && evtobj.ctrlKey) undoCanvas();
}

$(function(){
  renderGraph();
  document.onkeydown = keyPress;
})
