/* Global variables */
let punter = 0;
let num_punters = 3;
let json = undefined;

/* Graph rendering */

function renderGraph(graph) {
  punter = 0;
  console.log(graph);
  initCy(graph,
    function() {
      initialised = true;
      cy.edges().on("select", function(evt) { handleEdgeClick(this) } );
    }
  );
  return;
}

/* EVENT HANDLING LOGIC */

function handleEdgeClick(edge) {
  const source = edge.data("source");
  const target = edge.data("target");

  if (edge.data("owner") == undefined) {
    cy.edges().unselect();
    updateEdgeOwner(source, target);
    punter = (punter + 1) % num_punters;
  } else {
    console.log("That edge is already selected");
  }
}

function updateEdgeOwner(source, target) {
  const es = cy.edges("[source=\"" + source + "\"][target=\"" + target + "\"]")
  if (es.length > 0) {
    const e = es[0];
    e.data()["owner"] = punter;
    e.style("line-color", getPunterColour(punter));
  } else {
    console.log("Trying to update nonexistent edge! (" + source + " -- " + target + ")");
  }
}

function updateNumPunters() {
  num_punters = $("#num_punters").val();
  console.log(num_punters);
  cy.destroy();
  renderGraph(json);
}

/* MAIN PROCEDURE */
function loadMapList(showFirst) {
  fetch("maps.json", {mode: "no-cors"})
  .then(function(res) {
    return res.json() })
  .then(function(json) {
    const select_elem = $("#maps-select");
    const maps = json.maps;

    for (let i = 0; i < maps.length; i++) {
      const map = maps[i];
      const opt = new Option(map.name + " (" + map.num_nodes + " sites and " + map.num_edges + " rivers )", map.filename);
      select_elem.append(opt);
    }

    select_elem.change(function(evt) {
      const item = select_elem.find(":selected");
      //alert("selected " + item.text() + ", val: " + item.val());
      selectMap(item.val());
    } );

    if (showFirst) {
      selectMap(maps[0].filename);
    }
  });
}

function selectMap(url) {
  fetch(url, {mode: "no-cors"})
  .then(function(res) {
    return res.json()
  }).then(function(_json) {
    if (cy.elements !== undefined) {
      cy.destroy();
    }
    json = _json;
    renderGraph(json);
  });
  $("#download-link").attr("href", url);
}

function doVisualize() {
  json = JSON.parse($("#json-form").val());
  if (cy.elements !== undefined) {
    cy.destroy();
  }
  renderGraph(json);
}

$(function(){
  let logUrl = decodeURIComponent(location.search).substr(5);
  if (logUrl.length == 0) {
    const matches = /map=([^&#=]*)/.exec(window.location.search);
    if (matches !== null && matches !== undefined) {
      const param1 = matches[1];
      loadMapList(false);
      selectMap(param1);
    } else {
      loadMapList(true);
    }
  } else {
    loadMapList(false);
  }
})
