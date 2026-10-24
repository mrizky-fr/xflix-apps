// Jembatan kontrol video embed
(function () {
  'use strict';
  if (window.__xflixBridge) return;
  window.__xflixBridge = true;
  var bridge = window.XflixBridge;
  if (!bridge || typeof bridge.postMessage !== 'function') return;

  var frame = Math.random().toString(36).slice(2);
  var main = null;
  var ticks = 0;
  var CAPTIONS = ['.jw-captions', '.jw-text-track-container', '.ytp-caption-window-container',
    '.vjs-text-track-display', '.plyr__captions', '.art-subtitle'];
  var BIG_PLAY = '.jw-display-icon-display,.vjs-big-play-button,.plyr__control--overlaid,.ytp-large-play-button';

  // Sisakan video dan subtitle saja
  function soloCss() {
    var shown = CAPTIONS.map(function (s) {
      return 'html.xflix-solo ' + s + ',html.xflix-solo ' + s + ' *';
    }).join(',');
    return 'html.xflix-solo,html.xflix-solo body{background:#000!important}' +
      'html.xflix-solo body *{visibility:hidden!important}' +
      shown + '{visibility:visible!important}' +
      'html.xflix-solo video[data-xflix]{visibility:visible!important;position:fixed!important;' +
      'left:0!important;top:0!important;width:100%!important;height:100%!important;' +
      'max-width:none!important;max-height:none!important;margin:0!important;' +
      'object-fit:contain!important;background:#000!important}';
  }

  function solo() {
    var root = document.documentElement;
    if (!root || root.classList.contains('xflix-solo')) return;
    var style = document.createElement('style');
    style.textContent = soloCss();
    (document.head || root).appendChild(style);
    root.classList.add('xflix-solo');
  }

  function num(x) {
    return typeof x === 'number' && isFinite(x) ? x : 0;
  }

  // Video yang main dan terpanjang
  function score(v) {
    return (v.paused ? 0 : 1e7) + (v.duration === Infinity ? 1e6 : num(v.duration));
  }

  function pick() {
    var list = document.getElementsByTagName('video');
    var best = null;
    var top = -1;
    for (var i = 0; i < list.length; i++) {
      var s = score(list[i]);
      if (s > top) { best = list[i]; top = s; }
    }
    if (best !== main) {
      if (main) main.removeAttribute('data-xflix');
      if (best) best.setAttribute('data-xflix', '');
      main = best;
    }
    return top;
  }

  function bufferedEnd(v) {
    var t = v.currentTime;
    var ranges = v.buffered;
    for (var i = 0; i < ranges.length; i++) {
      if (ranges.start(i) <= t + 1 && ranges.end(i) >= t) return ranges.end(i);
    }
    return t;
  }

  function send() {
    var k = pick();
    var v = main;
    if (!v) {
      // Pemutar belum membuat video
      if (jw() || document.querySelector(BIG_PLAY)) {
        bridge.postMessage(JSON.stringify({ f: frame, k: 0.5, p: 0, d: 0, b: 0, on: false, w: false, e: false }));
      }
      return;
    }
    if (!v.paused && v.currentTime > 0) {
      v.controls = false;
      solo();
    }
    bridge.postMessage(JSON.stringify({
      f: frame,
      k: k,
      p: num(v.currentTime),
      d: v.duration === Infinity ? -1 : num(v.duration),
      b: num(bufferedEnd(v)),
      on: !v.paused,
      w: !v.paused && (v.seeking || v.readyState < 3),
      e: v.ended
    }));
  }

  function jw() {
    try {
      var p = typeof window.jwplayer === 'function' ? window.jwplayer() : null;
      return p && typeof p.play === 'function' ? p : null;
    } catch (e) {
      return null;
    }
  }

  function play() {
    var v = main;
    if (v && (v.currentSrc || v.readyState > 0)) {
      var r = v.play();
      if (r && r.catch) r.catch(function () {});
      return;
    }
    var p = jw();
    if (p) { p.play(); return; }
    var button = document.querySelector(BIG_PLAY);
    if (button) button.click();
  }

  bridge.onmessage = function (event) {
    var m;
    try { m = JSON.parse(event.data); } catch (e) { return; }
    var v = main;
    if (m.c === 'play') play();
    else if (!v) return;
    else if (m.c === 'pause') v.pause();
    else if (m.c === 'seek' && isFinite(m.v)) v.currentTime = Math.max(0, m.v);
    else if (m.c === 'rate' && m.v > 0) v.playbackRate = m.v;
    setTimeout(send, 80);
  };

  setInterval(function () {
    ticks++;
    try { send(); } catch (e) {}
  }, 500);
})();
