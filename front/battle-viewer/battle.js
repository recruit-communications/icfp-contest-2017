/* GLOBALS */
let punterId = -1;
let numPunters = -1;
let initialised = false;

let row = 0;
let col = 0;
let jsons = undefined;
let moves = undefined;
let doPlay = false;
let timeSpan = 500;
let splurges = [];
let canSplurge = false;
let canFeature = false;

/* Graph rendering */

function renderGraph(graph) {
  initCy(graph,
    function() {
      initialised = true;
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

function getRank(scores) {
  rank = [];
  for (let i = 0; i < scores.length; i++) rank.push(1);
  for (let i = 0; i < scores.length; i++) {
    for (let j = 0; j < scores.length; j++) {
      if (scores[i].score < scores[j].score)
        rank[i]++;
    }
  }
  return rank;
}

function setScores(scores, punterId) {
  $("#game-scores").append("<tr><th>punter No</th><th>Score</th><th>Rank</th><th>Splurge</th></tr>");
  let rank = getRank(scores);
  for (let i = 0; i < scores.length; i++) {
    $("#game-scores").append("<tr><td class=\"c0 punter" + scores[i].punter + "\" >punter #" + scores[i].punter + (punterId - scores[i].punter == 0 ? " (You)":"")
      + "</td><td class='c1'>" + scores[i].score + "</td><td class='c2'>" + rank[i] +"</td><td class='c3' id='splurge" + scores[i].punter + "'>" + splurges[scores[i].punter] + "</td></tr>");
    $(".punter" + i).css({"background-color":colours[scores[i].punter]});
  }
}

function updateSplurge(punterId) {
  if (canSplurge) {
    console.log(punterId, splurges[punterId]);
    $("#splurge" + punterId).text(splurges[punterId]);
  }
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
  writeLog("pass: punter #" + pass.punter + (canSplurge ? (" splurge gage increased to " + splurges[pass.punter]):"") + ".");
  return;
}

function logSplurge(splurge) {
  console.log(splurge);
  writeLog("splurge: punter #" + splurge.punter + " splurge " + (splurge.route.length - 1) + " edges and splurge gage decreased to " + splurges[splurge.punter] + ".");
}

function logScore(punter_id, score) {
  writeLog("punter #" + punter_id + " scored " + score);
}

function logMove(move) {
  if (move.claim != undefined) {
    logClaim(move.claim);
  } else if (move.pass != undefined) {
    logPass(move.pass);
  } else if (move.splurge != undefined) {
    logSplurge(move.splurge);
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

function logUnSplurge(splurge) {
  writeLog("unsplurge: punter #" + splurge.punter + ".");
}

function logRemove(move) {
  if (move.claim != undefined) {
    logUnClaim(move.claim);
  } else if (move.pass != undefined) {
    logUnPass(move.pass);
  } else if (move.splurge != undefined) {
    logUnSplurge(move.splurge);
  }
}

function logError(msg) {
  writeLog("error: " + msg);
  return;
}

function start() {
  let graph = undefined;
  let move_start = 0;
  moves = undefined;
  row = 0;
  col = 0;
  punterId = -1;
  doPlay = false;
  $("#game-scores").empty();
  bindResetHandlers();

  try {
    for (let i = 0; i < jsons.length; i++) {
      // Read settings (punters, map)
      let battleEnv = JSON.parse(jsons[i].substring(5));

      if (battleEnv.punter == undefined) continue;

      punterId = battleEnv.punter;
      numPunters = battleEnv.punters;

      if (battleEnv.settings != undefined) {
        if (battleEnv.settings.features != undefined)
          canReature = battleEnv.settings.features;
        if (battleEnv.settings.splurges != undefined)
          canSplurge = battleEnv.settings.splurges;
      }
      splurges = []
      for (let i = 0; i < numPunters; i++) {
        splurges.push(canSplurge ? 0:"-");
      }

      logInfo("number of punters: " + numPunters);
      logInfo("received initial game graph: " + JSON.stringify(battleEnv.map));
      graph = {
        "sites": battleEnv.map.sites,
        "rivers": battleEnv.map.rivers,
        "mines": battleEnv.map.mines,
      };
      logInfo("rendering game graph...");
      renderGraph(graph);
      logInfo("You are punter #" + punterId);
      logInfo("Features: " + canFeature + ",   Splurges: " + canSplurge);
      move_start = i + 1;
      break;
    }

    if (punterId == -1) {
      logError("no battle enviroment line!!!");
      return;
    }

    // Read scores and print final scores
    let stop = JSON.parse(jsons[jsons.length - 1].substring(5)).stop;
    let scores = stop.scores;
    if (scores == undefined) {
      logError("no scores values!!!");
      return;
    }
    setScores(scores, punterId);

    // Read moves and process battle play log
    moves = [];
    let firstMove = true;
    for (let i = move_start; i < jsons.length - 1; i++) {

      let json = JSON.parse(jsons[i].substring(5));
      if (json.move == undefined) continue;
      let move = [];
      for (let j = 0; j < numPunters; j++) {
        let punter = (punterId + j) % numPunters;
        //console.log(punter);
        for (let k = 0; k < json.move.moves.length; k++) {
          let ele = json.move.moves[k];
          if (ele.claim != undefined && ele.claim.punter == punter) {
            move.push(ele);
            break;
          }
          if (ele.pass != undefined && ele.pass.punter == punter) {
            if (firstMove && punterId <= punter) continue;
            move.push(ele);
            break;
          }
          if (ele.splurge != undefined && ele.splurge.punter == punter) {
            move.push(ele);
            break;
          }
        }
      }
      if (move.length > 0) moves.push(move);
      firstMove = false;
    }
    let move = [];
    for (let i = 0; i < stop.moves.length; i++) {
      if (stop.moves[i].claim !== undefined) {
        if (stop.moves[i].claim.punter < punterId) continue;
        move.push(stop.moves[i]);
      } else if (stop.moves[i].pass !== undefined) {
        if (stop.moves[i].pass.punter < punterId) continue;
        move.push(stop.moves[i]);
      } else if (stop.moves[i].splurge != undefined) {
        if (stop.moves[i].splurge.punter < punterId) continue;
        move.push(xtop.moves[i]);
      }
    }
    if (move.length > 0) moves.push(move);
    if (moves.length == 0) {
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
  start();
}

function handleBack() {
  bindBackHandlers();

  //console.log(row, col);
  if (row == 0 && col == 0) return;

  col--;
  if (col < 0) {
    row--;
    col = moves[row].length - 1;
  }

  //console.log(row, col);
  let data = moves[row][col];
  if (data.claim != undefined) {
    removeEdgeOwner(data.claim.punter, data.claim.source, data.claim.target);
  } else if (data.pass != undefined) {
    splurges[data.pass.punter]--;
    updateSplurge(data.pass.punter);
  } else if (data.splurge !=undefined) {
    let source = data.splurge.route[0];
    for (let i = 1; i < data.splurge.route.length; i++) {
      let target = data.splurge.route[i];
      removeEdgeOwner(data.splurge.puter, source, target);
      source = target;
    }
    splurges[data.splurge.punter] += data.splurge.route.length - 2;
    updateSplurge(data.splurge.punter);
  }
  logRemove(data);

  if (row == 0 && col == 0)
    handleReset(moves);
  else
    handlePause(moves);
}

function handleGo() {
  bindGoHandlers();
  forwardBattle(true);
  handlePause();
}

function forwardBattle(logging) {
  if (row >= moves.length) return;

  let data = moves[row][col];
  if (data.claim != undefined) {
    updateEdgeOwner(data.claim.punter, data.claim.source, data.claim.target);
  } else if (data.pass != undefined) {
    splurges[data.pass.punter]++;
    updateSplurge(data.pass.punter);
  } else if (data.splurge != undefined) {
    let source = data.splurge.route[0];
    for (let i = 1; i < data.splurge.route.length; i++) {
      let target = data.splurge.route[i];
      updateEdgeOwner(data.splurge.punter, source, target)
      source = target;
    }
    // splurgeは(指定した辺の数 - 1)だけsplurge gageを消費する。
    splurges[data.splurge.punter] -= data.splurge.route.length - 2;
    if (logging) {
      updateSplurge(data.splurge.punter);
    }
  }

  if (logging) {
    logMove(data);
  }

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
      setTimeout(play, timeSpan);
    } else if (row == moves.length){
      handleEnd();
    }
  })();
}

function handleEnd() {
  while (row < moves.length) {
    forwardBattle(false);
  }
  for (let i = 0; i < numPunters; i++) updateSplurge(i);
  logInfo("Moves End...");
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
    e.data()["owner"] = punter;
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
      return res.text();
    }).then(function(text) {
      return text.split("\n");
    }).then(function(_jsons) {
      if (cy.elements !== undefined) {
        cy.destroy();
      }
      jsons = _jsons;
      while (jsons[jsons.length - 1] == "") jsons.pop();
      start(jsons);
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

function doVisualize() {
  text = $("#json-form").val();

  if (text.length == 0) return;

  jsons = text.split("\n");
  if (cy.elements !== undefined) {
    cy.destroy();
  }
  while (jsons[jsons.length - 1] == "") jsons.pop();
  start();
}

function updateSpan() {
  timeSpan = $("#time-span").val();
  console.log("updateSpan with" + timeSpan + "ms");
}

$(function() {
  $(document).ready(function() {
    initPunterColours();
    logUrl = decodeURIComponent(location.search).substr(5);
    if (logUrl.length == 0) {
      const matches = /battle=([^&#=]*)/.exec(window.location.search);
      if (matches !== null && matches !== undefined) {
        const param1 = matches[1];
        loadBattleList(false);
        selectBattle(param1);
      } else {
        loadBattleList(true);
      }
    } else {
      loadBattleList(false);
    }
  });
});
