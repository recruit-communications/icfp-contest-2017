function formatDate(date) {
  const ff = (f) => ('0'+f).slice(-2);
  const MM = ff(date.getMonth() + 1)
  const DD = ff(date.getDate())
  const HH = ff(date.getHours())
  const mm = ff(date.getMinutes())
  const ss = ff(date.getSeconds());
  return `${MM}/${DD} ${HH}:${mm}:${ss}`
}

function dialog(status) {
  let params = {};
  if (status === 'success') {
    return '';
  } else if (status === 'fail') {
    params = {
      type: 'danger',
      msg: '対戦エラー'
    };
  } else {
    // 進行中？
    params = {
      type: 'info',
      msg: '対戦中'
    };
  }
  return `<div class="alert alert-${params.type}" role="alert">${params.msg}</div>`;
}

function calcRGB(a,b,m,n) {
  const ary = [];
  for(var i=0; i<3; i++){
    ary.push(((n*a[i]+m*b[i])/(m+n)).toFixed());
  }
  return "rgb(" + ary.join(",") + ")";
}

function changeColor(elm) {
  const w = [150,216,160];
  const d = [255,255,255];
  const l = [238,150,144];

  elm.each((i, e) => {
    const rate = $(e).text();
    if (rate > 0.500) {
      m = Math.pow((rate-0.5)/0.5, 0.6);
      $(e).css("background-color", calcRGB(d,w,m,1-m));
    } else if (typeof rate !== "undefined") {
      n = Math.pow((0.5-rate)/0.5, 0.6);
      $(e).css("background-color", calcRGB(l,d,1-n,n));
    }
  });
}
