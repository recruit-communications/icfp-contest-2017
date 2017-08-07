const colours =
  ["#aec7e8",
    "#ff7f0e",
    "#f7b6d2",
    "#1f77b4",
    "#2ca02c",
    "#c5b0d5",
    "#ffbb78",
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

function initPunterColours() {
  for (let i = 0; i < colours.length; i++) {
    $("#punter-colours").append("<div class=\"colours" + i + "\">" +("  " + i).substr(-2).replace(/ /g, "&ensp;") + "&nbsp;</div>");
    $(".colours" + i).css({"background-color":colours[i], "display":"inline"});
  }
}

function changeIdShow() {

}

function changeIdSize() {

}

function changeLineWidth() {
  width = $("#line-width").val();
  $("#cy .edge").css("width", width);
  console.log("updateWidth with" + width);
}

function getPngNameFromUrl() {
  let pngName = "image.png";
  var logUrl = decodeURIComponent(location.search).substr(5);
  if (logUrl.length > 0) {
    let paths = logUrl.split("/");
    let jsonName = paths[paths.length - 1];
    pngName = jsonName.substr(0, jsonName.length - 5) + ".png";
  }
  return pngName;
}

function handleDownloadPng() {
  let pngName = getPngNameFromUrl();
  let png = cy.png({bg:"black"});

  console.log(png);

  $("#download").attr("download", pngName);
  $("#download").attr("href", png);
}