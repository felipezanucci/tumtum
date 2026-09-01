import React from "react";
export function WordmarkPlate({ on = "black", width = 120, assetsPath = "assets", style, ...rest }) {
  const src = on === "black"
    ? assetsPath + "/tumtum-wordmark-white.svg"
    : assetsPath + "/tumtum-wordmark-black.svg";
  return <img {...rest} src={src} alt="TUMTUM" style={{ width, display: "block", ...style }} />;
}
