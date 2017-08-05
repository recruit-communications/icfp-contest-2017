const line = "line";
const mine = "mine";
let mode = "line";

let select = 1;
let id = 2;
let eles = [];     // cytograph 操作データ
let changes = [];  // 内部用の操作データ
let json = {
  "sites": [{"id":0, "x":0.0, "y":0.0}, {"id":1, "x":0.1, "y":0.0}],
  "rivers": [],
  "mines": [],
}

function initParams() {
  id = 2;
  select = 1;
  eles = [];
  changes = [];
}

function drawCanvas() {
  initParams();
  if (cy.elements !== undefined) {
    cy.destroy();
  }

  initCy(json, function () {
    cy.on('tap', function(event){
      var evtTarget = event.target;

      if(evtTarget === cy){
        console.log(event);

        let position = {
            x: event.originalEvent.offsetX,
            y: event.originalEvent.offsetY,
        };
        clickBackground(position);
      } else if (evtTarget.isNode()) {
        clickNode(evtTarget.id(), event.originalEvent.shiftKey);
      }
    });

    cy.on('drug', 'node', function(event) {
      var evtTarget = event.target;

      let position = {
        x: event.originalEvent.offsetX,
        y: event.originalEvent.offsetY,
      };
    });
  });
}

function clickBackground(position) {
  if (mode == line) {
    eles.push(cy.add([
      {group:"nodes", data: {"id": id}, renderedPosition: {x: position.x, y: position.y}},
      {group:"edges", data: {source: select, target: id}}
    ]));
    changes.push({mode:line, source:select, target:id, addNode:true});
    console.log("Added line:")
    console.log(eles[eles.length - 1]);
    select = id;
    id++;
  }
}

function clickNode(nodeId, shiftKey) {
  if (mode == line) {
    pre_select = select;
    select = nodeId;
    // shiftKeyが押されている場合は、selectが変わるだけ
    console.log("shiftKey: " + shiftKey + ", source: " + select + " target: " + pre_select);
    if (!shiftKey) {
      eles.push(cy.add([
        {group: "edges", data: {source:pre_select, target:select}}
      ]))
      changes.push({mode:line, source:pre_select, target:select, addNode:false});
    }
  } else {
    if (!cy.$id(nodeId).hasClass(mine)) {
      cy.$id(nodeId).addClass(mine);
      changes.push({mode:mine, id:nodeId, doing:"add"});
      console.log("Added mine:")
      console.log(changes[changes.length - 1]);
    } else {
      cy.$id(nodeId).removeClass(mine);
      changes.push({mode:mine, id:nodeId, doing:"remove"});
      console.log("Removed mine:")
      console.log(changes[changes.length - 1]);
    }
  }
}

function undoCanvas() {
  if (changes.length > 0) {
    let change = changes.pop();
    if (change.mode == line) {
      cy.remove(eles.pop());
      select = change.source;
      if (change.addNode) {
        id--;
      }
      console.log("Undo line:");
    } else {
      if (change.doing == "add") {
        cy.$id(change.id).removeClass(mine);
        console.log("Undo add mine:");
      } else {
        cy.$id(change.id).addClass(mine);
        console.log("Undo remove mine:");
      }
    }
    console.log(change);
  }
}

function resetCanvas() {
  cy.destroy();
  drawCanvas();
}

function lineInputMode() {
  mode = line;
  $("#mine-input").removeAttr("disabled");
  $("#line-input").attr("disabled", true);
}

function mineInputMode() {
  mode = mine;
  $("#line-input").removeAttr("disabled");
  $("#mine-input").attr("disabled", true);
}

function removeMode() {
  // TODO;
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

$(function(){
  drawCanvas();
})
