const IV = [
  0x7380166f, 0x4914b2b9, 0x172442d7, 0xda8a0600,
  0xa96f30bc, 0x163138aa, 0xe38dee4d, 0xb0fb0e4e,
];

const T = Array.from({ length: 64 }, (_, index) => (index < 16 ? 0x79cc4519 : 0x7a879d8a));

const rotl = (value: number, bits: number) =>
  ((value << bits) | (value >>> (32 - bits))) >>> 0;

const p0 = (value: number) => (value ^ rotl(value, 9) ^ rotl(value, 17)) >>> 0;
const p1 = (value: number) => (value ^ rotl(value, 15) ^ rotl(value, 23)) >>> 0;

const ff = (x: number, y: number, z: number, index: number) =>
  index < 16 ? (x ^ y ^ z) >>> 0 : ((x & y) | (x & z) | (y & z)) >>> 0;

const gg = (x: number, y: number, z: number, index: number) =>
  index < 16 ? (x ^ y ^ z) >>> 0 : ((x & y) | (~x & z)) >>> 0;

const pad = (bytes: Uint8Array) => {
  const bitLength = bytes.length * 8;
  const paddingLength = (56 - ((bytes.length + 1) % 64) + 64) % 64;
  const result = new Uint8Array(bytes.length + 1 + paddingLength + 8);
  result.set(bytes);
  result[bytes.length] = 0x80;

  const view = new DataView(result.buffer);
  const high = Math.floor(bitLength / 0x100000000);
  const low = bitLength >>> 0;
  view.setUint32(result.length - 8, high);
  view.setUint32(result.length - 4, low);
  return result;
};

const blockWord = (block: Uint8Array, offset: number, index: number) =>
  (
    (block[offset + index * 4] << 24) |
    (block[offset + index * 4 + 1] << 16) |
    (block[offset + index * 4 + 2] << 8) |
    block[offset + index * 4 + 3]
  ) >>> 0;

const compress = (state: number[], block: Uint8Array, offset: number) => {
  const w = new Array<number>(68);
  const wp = new Array<number>(64);

  for (let index = 0; index < 16; index += 1) {
    w[index] = blockWord(block, offset, index);
  }
  for (let index = 16; index < 68; index += 1) {
    w[index] = (
      p1(w[index - 16] ^ w[index - 9] ^ rotl(w[index - 3], 15)) ^
      rotl(w[index - 13], 7) ^
      w[index - 6]
    ) >>> 0;
  }
  for (let index = 0; index < 64; index += 1) {
    wp[index] = (w[index] ^ w[index + 4]) >>> 0;
  }

  let [a, b, c, d, e, f, g, h] = state;
  for (let index = 0; index < 64; index += 1) {
    const ss1 = rotl((rotl(a, 12) + e + rotl(T[index], index % 32)) >>> 0, 7);
    const ss2 = (ss1 ^ rotl(a, 12)) >>> 0;
    const tt1 = (ff(a, b, c, index) + d + ss2 + wp[index]) >>> 0;
    const tt2 = (gg(e, f, g, index) + h + ss1 + w[index]) >>> 0;
    d = c;
    c = rotl(b, 9);
    b = a;
    a = tt1;
    h = g;
    g = rotl(f, 19);
    f = e;
    e = p0(tt2);
  }

  return [
    (state[0] ^ a) >>> 0,
    (state[1] ^ b) >>> 0,
    (state[2] ^ c) >>> 0,
    (state[3] ^ d) >>> 0,
    (state[4] ^ e) >>> 0,
    (state[5] ^ f) >>> 0,
    (state[6] ^ g) >>> 0,
    (state[7] ^ h) >>> 0,
  ];
};

export const sm3Bytes = (bytes: Uint8Array) => {
  const padded = pad(bytes);
  let state = [...IV];
  for (let offset = 0; offset < padded.length; offset += 64) {
    state = compress(state, padded, offset);
  }
  return state.flatMap((word) => [
    (word >>> 24) & 0xff,
    (word >>> 16) & 0xff,
    (word >>> 8) & 0xff,
    word & 0xff,
  ]);
};

export const sm3Hex = (bytes: Uint8Array) =>
  sm3Bytes(bytes).map((byte) => byte.toString(16).padStart(2, "0")).join("");

export const sm3Base64 = (bytes: Uint8Array) => {
  const binary = String.fromCharCode(...sm3Bytes(bytes));
  return btoa(binary);
};
