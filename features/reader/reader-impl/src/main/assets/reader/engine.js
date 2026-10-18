'use strict';
(()=>{
const content=document.getElementById('content'),viewport=document.getElementById('viewport');
let nodes=[],readable=[],nodeMap=new WeakMap(),publicationText='',length=0,settings={},annotations=[],lastOffset=0,page=0,ready=false,moving=false,scrollTimer,noteFrame=0,noteLabel='',notePositions=[];
const noteLayer=document.createElement('div');noteLayer.id='reader-note-markers';document.body.append(noteLayer);const noteButtons=new Map();
const colors={Amber:'#f3d55b65',Mint:'#7dc9a865',Sky:'#79bce465',Lavender:'#b19ae565',Rose:'#e79eb665',Peach:'#f1b17d65'};
function send(type,value){ReaderBridge.message(JSON.stringify({type,value}));}
function index(){nodes=[];nodeMap=new WeakMap();length=0;const walker=document.createTreeWalker(content,NodeFilter.SHOW_TEXT);let n;while(n=walker.nextNode()){const item={node:n,start:length,end:length+n.length};nodes.push(item);nodeMap.set(n,item);length+=n.length;}readable=nodes.filter(n=>n.node.textContent.trim());publicationText=content.textContent;}
function point(offset){offset=Math.max(0,Math.min(length,offset));let low=0,high=nodes.length;while(low<high){const mid=(low+high)>>1;if(nodes[mid].end<=offset)low=mid+1;else high=mid;}const n=nodes[low]||nodes[nodes.length-1];return n?{node:n.node,offset:Math.max(0,Math.min(n.node.length,offset-n.start))}:null;}
function offset(node,local){const n=nodeMap.get(node);return n?n.start+local:null;}
function range(start,end){const a=point(start),b=point(end);if(!a||!b)return null;const r=document.createRange();r.setStart(a.node,a.offset);r.setEnd(b.node,b.offset);return r;}
function text(){return publicationText;}
function currentOffset(){
 const margin=parseInt(settings.margin||20),visible=readable;let low=0,high=visible.length;
 // Двоичный поиск по текстовым узлам не обходит всю книгу при каждом движении.
 while(low<high){const mid=(low+high)>>1,r=document.createRange();r.selectNodeContents(visible[mid].node);const b=r.getBoundingClientRect();const before=settings.mode==='Pages'?b.right<margin+1:b.bottom<24;if(before)low=mid+1;else high=mid;}
 for(let i=low;i<Math.min(visible.length,low+24);i++){const n=visible[i],r=document.createRange();r.selectNodeContents(n.node);const box=Array.from(r.getClientRects()).find(b=>b.bottom>24&&b.top<innerHeight-24&&b.right>margin+1&&b.left<innerWidth-margin);if(!box)continue;
  const x=Math.max(margin+2,box.left+2),y=Math.min(box.bottom-1,Math.max(25,box.top+box.height/2));const caret=document.caretRangeFromPoint(x,y);if(caret&&content.contains(caret.startContainer)){const at=offset(caret.startContainer,caret.startOffset);if(at!==null)return at;}return n.start;
 }return lastOffset;
}
function count(){return settings.mode==='Pages'?Math.max(1,Math.ceil((viewport.scrollWidth-1)/innerWidth)):Math.max(1,Math.ceil(document.documentElement.scrollHeight/innerHeight));}
function report(){if(!ready||moving)return;renderNoteMarkers();lastOffset=currentOffset();const total=count();page=settings.mode==='Pages'?Math.round(viewport.scrollLeft/innerWidth):(scrollY+innerHeight>=document.documentElement.scrollHeight-2?total-1:Math.floor(scrollY/innerHeight));const atEnd=settings.mode==='Pages'?page>=total-1:scrollY+innerHeight>=document.documentElement.scrollHeight-2;const section=point(lastOffset)?.node.parentElement.closest('section');send('position',{offset:lastOffset,progression:atEnd?1:Math.min(.999,lastOffset/Math.max(1,length)),atEnd,chapterId:section?.dataset.chapter||'',chapter:section?.dataset.title||'',page:Math.min(total,page+1),pages:total});}
function go(offsetValue){let visible=offsetValue;const n=nodes.find(n=>n.end>offsetValue&&n.node.textContent.trim());if(n)visible=Math.max(offsetValue,n.start+(n.node.textContent.search(/\S/)));const r=range(visible,Math.min(length,visible+1));if(!r)return;moving=true;lastOffset=offsetValue;const box=r.getBoundingClientRect();if(settings.mode==='Pages'){viewport.scrollLeft=Math.floor((box.left+viewport.scrollLeft)/innerWidth)*innerWidth;}else{scrollTo(0,Math.max(0,box.top+scrollY-24));}requestAnimationFrame(()=>{moving=false;layoutNoteMarkers();report();});}
// WebView может обнулить CSS vh при смене окон; высоту колонок берём из текущего viewport.
function sizeViewport(){document.documentElement.style.setProperty('--reader-height',innerHeight+'px');}
function apply(value,restore){const previous=ready?currentOffset():lastOffset;sizeViewport();settings=value;const dark=value.theme==='Night',themes={Light:['#f4f4f4','#292834','#e8e7ed'],Paper:['#fcf6ec','#423c35','#eee6d8'],Sepia:['#ead4b5','#493b2c','#ddc7a7'],Night:['#1d1d20','#e1dfe8','#303036']};const t=themes[value.theme]||themes.Light;
 const style=document.body.style;style.setProperty('--paper',t[0]);style.setProperty('--ink',t[1]);style.setProperty('--formula',t[2]);style.setProperty('--muted',dark?'#aaa5b9':'#777382');style.setProperty('--rule',dark?'#55515f':'#ccc9d9');style.setProperty('--accent',dark?'#e1dfe8':'#48474c');style.setProperty('--margin',value.margin+'px');style.setProperty('--size',value.fontSize+'px');style.setProperty('--line',value.lineHeight);style.setProperty('--font',value.font==='Serif'?'Georgia':value.font==='Sans'?'sans-serif':'Nunito');document.documentElement.style.background=t[0];document.body.classList.toggle('paged',value.mode==='Pages');document.body.classList.toggle('justify',value.justify);document.body.classList.toggle('hide-formulas',!value.showFormulas);if(restore)requestAnimationFrame(()=>go(previous));}
function paint(values){annotations=values;const supported=!!(window.CSS&&CSS.highlights&&window.Highlight);if(supported){CSS.highlights.clear();for(const name of Object.keys(colors)){const ranges=values.filter(v=>v.color===name&&v.anchor.end<=length&&text().slice(v.anchor.start,v.anchor.end)===v.anchor.quote).map(v=>range(v.anchor.start,v.anchor.end)).filter(Boolean);CSS.highlights.set(name,new Highlight(...ranges));}}
 else{
 // Запасной путь для старого Android System WebView; текст и его UTF-16 адреса сохраняются.
 content.querySelectorAll('mark[data-reader-mark]').forEach(mark=>mark.replaceWith(...mark.childNodes));content.normalize();index();
 for(const value of values.slice().sort((a,b)=>b.anchor.start-a.anchor.start)){
  if(value.anchor.end>length||text().slice(value.anchor.start,value.anchor.end)!==value.anchor.quote)continue;
  for(const item of nodes.filter(n=>n.end>value.anchor.start&&n.start<value.anchor.end).reverse()){
   const start=Math.max(0,value.anchor.start-item.start),end=Math.min(item.node.length,value.anchor.end-item.start);
   if(end<=start)continue;const r=document.createRange();r.setStart(item.node,start);r.setEnd(item.node,end);const mark=document.createElement('mark');mark.dataset.readerMark=value.id;mark.style.backgroundColor=colors[value.color];r.surroundContents(mark);
  }index();
 }
 }layoutNoteMarkers();}
// Отметки находятся вне текста книги: они не меняют адреса выделений и вёрстку страниц.
function layoutNoteMarkers(){
 notePositions=annotations.filter(v=>v.note&&v.anchor.end<=length&&text().slice(v.anchor.start,v.anchor.end)===v.anchor.quote).map(v=>{const r=range(v.anchor.start,Math.min(v.anchor.start+1,v.anchor.end)),b=r?.getClientRects()[0];return b?{id:v.id,left:b.left+scrollX+viewport.scrollLeft,top:b.top+scrollY}:null;}).filter(Boolean);renderNoteMarkers();
}
function renderNoteMarkers(){
 noteFrame=0;noteLayer.style.visibility=getSelection().isCollapsed?'':'hidden';const visible=new Set();
 for(const value of notePositions){const x=value.left-scrollX-viewport.scrollLeft,y=value.top-scrollY;if(x<0||x>innerWidth||y<18||y>innerHeight)continue;visible.add(value.id);let button=noteButtons.get(value.id);
  if(!button){button=document.createElement('button');button.type='button';button.className='reader-note-marker';button.dataset.annotation=value.id;button.setAttribute('aria-label',noteLabel);button.onclick=e=>{e.stopPropagation();send('annotation',{id:value.id});};noteButtons.set(value.id,button);noteLayer.append(button);}
  button.style.left=Math.max(0,Math.min(innerWidth-32,x-5.5))+'px';button.style.top=(y-25)+'px';
 }
 for(const [id,button] of noteButtons){if(!visible.has(id)){button.remove();noteButtons.delete(id);}}
}
function scheduleNoteMarkers(){if(!noteFrame)noteFrame=requestAnimationFrame(renderNoteMarkers);}
document.addEventListener('selectionchange',scheduleNoteMarkers);
function selection(action){const s=getSelection();if(!s||s.isCollapsed||!s.rangeCount)return;const r=s.getRangeAt(0);if(!content.contains(r.startContainer)||!content.contains(r.endContainer))return;const start=offset(r.startContainer,r.startOffset),end=offset(r.endContainer,r.endOffset);if(start===null||end===null||start>=end||end-start>10000)return;const all=text(),section=r.startContainer.parentElement.closest('section');send('selection',{action,start,end,quote:all.slice(start,end),prefix:all.slice(Math.max(0,start-64),start),suffix:all.slice(end,end+64),chapterId:section?.dataset.chapter||''});s.removeAllRanges();}
window.Reader={settings:v=>apply(v,true),annotations:paint,selection,jump:go,chapter:id=>{const e=document.querySelector('section[data-chapter="'+id.replace(/[^a-zA-Z0-9_-]/g,'')+'"]');const n=nodes.find(n=>e?.contains(n.node)&&n.node.textContent.trim());if(n)go(n.start+n.node.textContent.search(/\S/));},step:delta=>{if(settings.mode!=='Pages')return;viewport.scrollLeft=Math.max(0,Math.min(count()-1,page+delta))*innerWidth;report();},position:report};
let downX=0,downY=0,downAt=0;content.addEventListener('pointerdown',e=>{downX=e.clientX;downY=e.clientY;downAt=Date.now();});content.addEventListener('click',e=>{if(Date.now()-downAt>350||Math.abs(e.clientX-downX)>12||Math.abs(e.clientY-downY)>12||!getSelection().isCollapsed)return;const a=e.target.closest('a[href]');if(a){e.preventDefault();const e2=document.getElementById(a.hash.slice(1));const n=nodes.find(n=>e2?.contains(n.node));if(n)go(n.start);return;}const r=document.caretRangeFromPoint(e.clientX,e.clientY),at=r?offset(r.startContainer,r.startOffset):null;const annotation=at===null?null:annotations.find(v=>at>=v.anchor.start&&at<=v.anchor.end);if(annotation)send('annotation',{id:annotation.id});else send('tap',{});});
// В постраничном режиме листаем только кнопками, жест не меняет экран и не закрывает книгу.
viewport.addEventListener('touchmove',e=>{if(settings.mode==='Pages'&&getSelection().isCollapsed&&!e.target.closest('.reader-formula'))e.preventDefault();},{passive:false});
function scrolled(){scheduleNoteMarkers();clearTimeout(scrollTimer);scrollTimer=setTimeout(report,150);}
window.addEventListener('scroll',scrolled,{passive:true});viewport.addEventListener('scroll',scrolled,{passive:true});window.addEventListener('resize',()=>{sizeViewport();if(ready)go(lastOffset);});
window.ReaderStart=async function(config){try{noteLabel=config.noteLabel;index();apply(config.settings,false);await Promise.all([document.fonts.load('17px Nunito'),document.fonts.load('italic 17px Nunito')]);await document.fonts.ready;await Promise.all(Array.from(content.querySelectorAll('img')).map(i=>i.complete?Promise.resolve():new Promise(resolve=>{i.onload=resolve;i.onerror=resolve;})));lastOffset=config.position?.offset??Math.round(length*(config.progression||0));ready=true;paint(config.annotations||[]);go(lastOffset);send('ready',{});}catch(e){send('error',{});}};
})();
