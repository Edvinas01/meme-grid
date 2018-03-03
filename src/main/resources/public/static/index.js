window.onload = () => {
  const grid = document.getElementById('grid');

  const insertImages = images => {
    grid.innerHTML = images
      .map(img => `
          <a class="meme-wrapper" href="${img.url}" target="_blank">
            <img class="meme" src="${img.url}" title="${img.title}"/>
          </a>
      `)
      .join('');
  };

  fetch('/api/memes')
    .then(res => res.json())
    .then(insertImages)
    .catch(() => {
      grid.innerText = 'Memes could not be fetched :(';
    });
};
