$(function() {
  // 親フレームからログの場所を受け取る
  var logUrl = decodeURIComponent(location.search).substr(5);

  if (logUrl) {
    $.ajax({
      url: logUrl,
      type: 'GET',
      dataType: 'text',
      crossDomain: true
    }).done(function(data) {
      $("#json-form").val(data);
      doVisualize();
    }); 
  }

  $('html').keydown(function(e) {
    console.log(e.which);
    switch(e.which) {
      case 39: // Key[->]
      case 190:
        handleGo();
        break;
      case 188:
      case 37: // Key[<-]
        handleBack();
        break;
    }   
  }); 
});
