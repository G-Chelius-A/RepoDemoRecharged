import { toByteArray } from 'base64-js';
import jpeg from 'jpeg-js';
import 'tracking/build/tracking.js';
import 'tracking/build/data/face-min.js';

const tracking = globalThis.tracking;

export function detectFaceFromBase64(base64) {
  const imageBytes = toByteArray(base64);
  const image = jpeg.decode(imageBytes, { useTArray: true });
  const scale = Math.min(1, 640 / Math.max(image.width, image.height));
  const width = Math.max(1, Math.round(image.width * scale));
  const height = Math.max(1, Math.round(image.height * scale));
  let pixels = image.data;

  if (scale < 1) {
    pixels = new Uint8Array(width * height * 4);
    for (let y = 0; y < height; y += 1) {
      const sourceY = Math.min(image.height - 1, Math.floor(y / scale));
      for (let x = 0; x < width; x += 1) {
        const sourceX = Math.min(image.width - 1, Math.floor(x / scale));
        const sourceIndex = (sourceY * image.width + sourceX) * 4;
        const targetIndex = (y * width + x) * 4;
        pixels[targetIndex] = image.data[sourceIndex];
        pixels[targetIndex + 1] = image.data[sourceIndex + 1];
        pixels[targetIndex + 2] = image.data[sourceIndex + 2];
        pixels[targetIndex + 3] = image.data[sourceIndex + 3];
      }
    }
  }

  const faces = tracking.ViolaJones.detect(
    pixels,
    width,
    height,
    1,
    1.25,
    2,
    0,
    tracking.ViolaJones.classifiers.face
  );

  return faces.length > 0;
}