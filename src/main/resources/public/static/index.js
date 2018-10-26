window.onload = () => {
  const styleButton = document.getElementById('style-switch');
  const preferList = 'true' === localStorage.getItem('prefer-list');
  const grid = document.getElementById('grid');

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

  const mapImages = images => {
    return images.map(image => {
      const {title, url, id} = image;

      const a = document.createElement('a');
      a.target = '_blank';
      a.href = url;

      const img = document.createElement('img');
      img.setAttribute('data-id', id);
      img.classList.add(memeClass);
      img.title = title;
      img.src = url;

      a.appendChild(img);

      return a;
    });
  };

  const showImages = images => {
    const masonry = preferList ? null : new Masonry(grid, {
      percentPosition: true,
      itemSelector: `.${memeClass}`,
      columnWidth: '.meme-sizer'
    });

    imagesLoaded(images).on('progress', (instance, image) => {
      if (!image.isLoaded) {
        return;
      }

      const imageWrapper = image.img.parentElement;
      grid.appendChild(imageWrapper);

      if (masonry) {
        masonry.appended(imageWrapper);
        masonry.layout();
      }
    });
  };

  let isLoading = false;
  let isCompleted = false;
  let currentPage = 0;

  const loadImages = () => {
    if (isLoading || isCompleted) return;
    isLoading = true;

    fetch('/api/memes/page/' + currentPage)
      .then(response => response.json())
      .then(images => {
        currentPage++;
        isCompleted = images.length === 0;
        if (isCompleted) {
          window.onscroll = null;
        }

        return images;
      })
      .then(mapImages)
      .then(showImages)
      .catch(() => {
        grid.innerText = 'Memes could not be fetched :(';
      })
      .finally(() => {
        isLoading = false;
      });
  };

  loadImages();

  window.onscroll = () => {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight) {
      loadImages();
    }
  }
};
