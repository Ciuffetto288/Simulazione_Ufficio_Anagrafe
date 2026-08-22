import { cp, mkdir, rm } from "node:fs/promises";

const root = new URL(".", import.meta.url).pathname;
const targets = ["chrome-edge-opera", "firefox"];

await Promise.all(targets.map(async (target) => {
  const directory = `${root}${target}`;
  await mkdir(directory, { recursive: true });
  await cp(`${root}dist/content.js`, `${directory}/content.js`);
  await cp(`${root}dist/popup.js`, `${directory}/popup.js`);
}));

await rm(`${root}dist`, { recursive: true, force: true });
console.log("Estensioni Chromium e Firefox aggiornate dai sorgenti TypeScript.");