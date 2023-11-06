const localMarked = new marked.Marked(
  markedHighlight.markedHighlight({
    langPrefix: 'hljs language-',
    highlight(code, lang) {
        console.log(`highlighing code with $lang`)
      const language = hljs.getLanguage(lang) ? lang : 'plaintext';
      return hljs.highlight(code, { language }).value;
    }
  })
);

function setMarkdown(markdown) {
    document.getElementById('content').innerHTML = marked.parse(markdown)
}