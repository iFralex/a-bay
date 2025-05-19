
import { addRoute, initRouter } from './router.js';
import { renderAuctionList } from './components/astaCard.js';
import { renderAuctionDetail } from './components/articoloCard.js';

document.addEventListener('DOMContentLoaded', () => {
  addRoute('home', renderAuctionList);
  addRoute('asta', renderAuctionDetail);
  initRouter();
});
