window.onload = () => {
  const styleButton = document.getElementById('style-switch');
  const preferList = 'true' === localStorage.getItem('prefer-list');

  if (preferList) {
    styleButton.innerText = 'I don\'t like lists';
  } else {
    styleButton.innerText = 'I don\'t like grids';
  }

  styleButton.addEventListener('click', () => {
    localStorage.setItem('prefer-list', (!preferList).toString());
    window.location.reload();
  });

  const memeClass = preferList
    ? 'list-meme'
    : 'meme';

  const grid = document.getElementById('grid');
  fetch('/api/memes')
    .then(res => res.json())
    .then(images => {
      grid.innerHTML += images
        .map(img => `
          <a href="${img.url}" target="_blank">
            <img class="${memeClass}" src="${img.url}" title="${img.title}"/>
          </a>
        `)
        .join('');
    })
    .then(() => {
      if (!preferList) {
        new Masonry(grid, {
          percentPosition: true,
          itemSelector: `.${memeClass}`,
          columnWidth: '.meme-sizer'
        });
      }
    })
    .catch(() => {
      grid.innerText = 'Memes could not be fetched :(';
    });
};
