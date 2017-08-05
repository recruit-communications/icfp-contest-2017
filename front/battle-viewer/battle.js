/* GLOBALS */
let numPunters = -1;
let initialised = false;

let row = 0;
let col = 0;
let moves = undefined;
let doPlay = false;

/* Graph rendering */

const colours =
  ["#aec7e8",
    "#ff7f0e",
    "#ffbb78",
    "#f7b6d2",
    "#1f77b4",
    "#2ca02c",
    "#c5b0d5",
    "#98df8a",
    "#d62728",
    "#ff9896",
    "#dbdb8d",
    "#9467bd",
    "#8c564b",
    "#c7c7c7",
    "#c49c94",
    "#e377c2",
    "#7f7f7f",
    "#bcbd22",
    "#17becf",
    "#9edae5"];

function getPunterColour(punter) {
  return colours[punter % colours.length];
}

function renderGraph(graph) {
  console.log(graph);
  initCy(graph,
    function() {
      initialised = true;
      cy.autolock(true);
      cy.edges().on("select", function(evt) {  } );
    }
  );
  return;
}

function toggleButton(buttonId, st) {
  $("#" + buttonId).attr("disabled", st);
}

function disableButton(buttonId) {
  toggleButton(buttonId, true);
}

function enableButton(buttonId) {
  toggleButton(buttonId, false);
}

/**
 * Communication
 */

function setScores(scores) {
  $("#game-scores").append("[Final Scores] ");
  $("#game-scores").css("font-size", "18px");
  for (let i = 0; i < scores.length; i++) {
    $("#game-scores").append("<div class=\"colours" + scores[i].punter + "\">punter #" + scores[i].punter + ": " + scores[i].score + "&nbsp;</div> ");
    $(".colours" + i).css({"background-color":colours[scores[i].punter], "display":"inline"});
  }
}

function getFormatedScores(scores) {
  let res = "[Final Scores] ";
  for (let i = 0; i < scores.length; i++) {
    res += "punter #" + scores[i].punter + ": " + scores[i].score;
    if (i < scores.length) res += ",  ";
  }
  return res;
}

function writeLog(msg) {
  let id = "log";
  document.getElementById(id).innerHTML += msg + "\n";
  document.getElementById(id).scrollTop = document.getElementById(id).scrollHeight;
  return;
}

function logInfo(msg) {
  writeLog("info: " + msg);
  return;
}

function logClaim(claim) {
  writeLog("move: punter #" + claim.punter + " claimed edge " +
    claim.source + " -- " + claim.target + ".");
  return;
}

function logPass(pass) {
  writeLog("pass: punter #" + pass.punter + ".");
  return;
}

function logScore(punter_id, score) {
  writeLog("punter #" + punter_id + " scored " + score);
}

function logMove(move) {
  if (move.claim != undefined) {
    logClaim(move.claim);
  } else if (move.pass != undefined) {
    logPass(move.pass);
  }
}

function logUnClaim(claim) {
  writeLog("unmove: punter #" + claim.punter + " claimed edge " +
    claim.source + " -- " + claim.target + ".");
  return;
}

function logUnPass(pass) {
  writeLog("unpass: punter #" + pass.punter + ".");
  return;
}

function logRemove(move) {
  if (move.claim != undefined) {
    logUnClaim(move.claim);
  } else if (move.pass != undefined) {
    logUnPass(move.pass);
  }
}

function logError(msg) {
  writeLog("error: " + msg);
  return;
}

function start(json) {
  let graph = undefined;
  moves = undefined;
  row = 0;
  doPlay = false;
  $("#game-scores").empty();
  bindResetHandlers();

  try {
    // Read settings (punters, map)
    let settings = json.settings;
    if (settings == undefined) {
      logError("no settings values!!!!");
      return;
    }
    //punterID = json.settings.punter;
    numPunters = settings.punters;
    logInfo("number of punters: " + numPunters);
    logInfo("received initial game graph: " + JSON.stringify(settings.map));
    graph = {
      "sites": settings.map.sites,
      "rivers": settings.map.rivers,
      "mines": settings.map.mines,
    };
    logInfo("rendering game graph...");
    renderGraph(graph);


    // Read scores and print final scores
    let scores = json.scores;
    if (scores == undefined) {
      logError("no scores values!!!");
      return;
    }
    setScores(scores);


    // Read moves and process battle play log
    moves = json.moves;
    if (moves == undefined) {
      logError("no moves values!!!");
      return;
    }

  } catch (e) { // other message from the server
    console.log(e);
    logError("received unknown data from json data.");
  }
  return;
};

/* EVENT HANDLING LOGIC */

function handleReset(moves) {
  logInfo("Reset viewer");
  selectBattle($("#download-link").attr("href"));
}

function handleBack() {
  bindBackHandlers();

  console.log(row, col);
  col--;
  if (col < 0) {
    row--;
    col = moves[row].length - 1;
  }

  console.log(row, col);
  let data = moves[row][col];
  if (data.claim != undefined) {
    removeEdgeOwner(data.claim.punter, data.claim.source, data.claim.target);
  }
  logRemove(data);

  if (row == 0 && col == 0)
    handleReset(moves);
  else
    handlePause(moves);
}

function handleGo() {
  bindGoHandlers();
  forwardBattle();
  handlePause();
}

function forwardBattle() {
  let data = moves[row][col];
  if (data.claim != undefined) {
    updateEdgeOwner(data.claim.punter, data.claim.source, data.claim.target);
  }
  logMove(data);

  col++;
  if (col == moves[row].length) {
    col = 0;
    row++;
  }

  if (row == moves.length)
    handleEnd();
}

function handlePause() {
  bindPauseHandlers();
  doPlay = false;
}

function handlePlay() {
  bindPlayHandlers();
  doPlay = true;
  (function play() {
    console.log("DOPLAY");
    if (doPlay && row < moves.length) {
      forwardBattle();
      setTimeout(play, 500);
    } else if (row == moves.length){
      handleEnd();
    }
  })();
}

function handleEnd() {
  while (row < moves.length) {
    handleGo(moves);
  }
  bindEndHandlers();
}

function bindResetHandlers() {
  $("#go-button").removeAttr("disabled");
  $("#play-button").removeAttr("disabled");
  $("#end-button").removeAttr("disabled");
  $("#reset-button").attr("disabled", true);
  $("#back-button").attr("disabled", true);
  $("#pause-button").attr("disabled", true);
}

function bindBackHandlers() {
  $("#go-button").removeAttr("disabled");
  $("#play-button").removeAttr("disabled");
  $("#end-button").removeAttr("disabled");
}

function bindGoHandlers() {
  $("#reset-button").removeAttr("disabled");
  $("#back-button").removeAttr("disabled");
}

function bindEndHandlers() {
  $("#reset-button").removeAttr("disabled");
  $("#back-button").removeAttr("disabled");
  $("#pause-button").removeAttr("disabled");
  $("#go-button").attr("disabled", true);
  $("#play-button").attr("disabled", true);
  $("#end-button").attr("disabled", true);
  $("#pause-button").attr("disabled", true);
}

function bindPlayHandlers() {
  $("#reset-button").removeAttr("disabled");
  $("#back-button").removeAttr("disabled");
  $("#pause-button").removeAttr("disabled");
  $("#play-button").attr("disabled", true);
  $("#pause-button").removeAttr("disabled");
}

function bindPauseHandlers() {
  $("#pause-button").attr("disabled", true);
  $("#play-button").removeAttr("disabled");
}

/* GAME UPDATE LOGIC */

function updateEdgeOwner(punter, source, target) {
  let es = cy.edges("[source=\"" + source + "\"][target=\"" + target + "\"]");
  if (es.length == 0) es = cy.edges("[source=\"" + target + "\"][target=\"" + source + "\"]");

  if (es.length > 0) {
    const e = es[0];
    e.data()["owner"] = undefined;
    e.style("line-color", getPunterColour(punter));
  } else {
    logError("Trying to update nonexistent edge! (" + source + " -- " + target + ")");
  }
}

function removeEdgeOwner(punter, source, target) {
  let es = cy.edges("[source=\"" + source + "\"][target=\"" + target + "\"]")
  if (es.length == 0) es = cy.edges("[source=\"" + target + "\"][target=\"" + source + "\"]")
  if (es.length > 0) {
    const e = es[0];
    e.data()["owner"] = undefined;
    e.style("line-color", "#009");
  } else {
    logError("Trying to remove nonexistent edge! (" + source + " -- " + target + ")");
  }
}

function selectBattle(url) {
  fetch(url, {mode: "no-cors"})
    .then(function(res) {
      return res.json()
    }).then(function(json) {
      if (cy.elements !== undefined) {
        cy.destroy();
      }
      start(json);
  });
  $("#download-link").attr("href", url);
}

function loadBattleList(showFirst) {
  fetch("battles.json", {mode: "no-cors"})
    .then(function(res) {
      return res.json() })
    .then(function(json) {
      const select_elem = $("#battles-select");
      const battles = json.battles;

      for (let i = 0; i < battles.length; i++) {
        const battle = battles[i];
        const opt = new Option(battle.name + " (" + battle.num_nodes + " sites and " + battle.num_edges + " rivers, " + battle.num_punters + " punters)", battle.filename);
        select_elem.append(opt);
      }

      select_elem.change(function(evt) {
        const item = select_elem.find(":selected");
        //alert("selected " + item.text() + ", val: " + item.val());
        selectBattle(item.val());
      } );

      if (showFirst) {
        selectBattle(battles[0].filename);
      }
    });
}

function initPunterColours() {
  for (let i = 0; i < colours.length; i++) {
    $("#punter-colours").append("<div class=\"colours" + i + "\">&ensp;&nbsp;" + i + "&ensp;</div>");
    $(".colours" + i).css({"background-color":colours[i], "display":"inline"});
  }
}

$(function() {
  $(document).ready(function() {
    initPunterColours();
    const matches = /battle=([^&#=]*)/.exec(window.location.search);
    if (matches !== null && matches !== undefined) {
      const param1 = matches[1];
      loadBattleList(false);
      selectBattle(param1);
    } else {
      loadBattleList(true);
    }
  });
});

