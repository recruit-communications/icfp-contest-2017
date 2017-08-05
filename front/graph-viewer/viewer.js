/* Global variables */
let punter = 0;
let num_punters = 3;

/* Graph rendering */

const colours =
  ["#1f77b4",
    "#aec7e8",
    "#ff7f0e",
    "#ffbb78",
    "#2ca02c",
    "#98df8a",
    "#d62728",
    "#ff9896",
    "#9467bd",
    "#c5b0d5",
    "#8c564b",
    "#c49c94",
    "#e377c2",
    "#f7b6d2",
    "#7f7f7f",
    "#c7c7c7",
    "#bcbd22",
    "#dbdb8d",
    "#17becf",
    "#9edae5"];

function renderGraph(graph) {
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

function getPunterColour(punter) {
  return colours[punter % colours.length];
}

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
  punter = 0;
  num_punters = $("#num_punters").val();
  console.log(num_punters);
  cy.destroy();

  selectMap($("#download-link").attr("href"));
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
  }).then(function(json) {
    if (cy.elements !== undefined) {
      cy.destroy();
    }
    renderGraph(json);
  });
  $("#download-link").attr("href", url);
}

$(function(){
  const matches = /map=([^&#=]*)/.exec(window.location.search);
  if (matches !== null && matches !== undefined) {
    const param1 = matches[1];
    loadMapList(false);
    selectMap(param1);
  } else {
    loadMapList(true);
  }
})
