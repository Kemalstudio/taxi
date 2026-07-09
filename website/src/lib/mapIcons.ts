import L from "leaflet";

function dot(cls: string, size: number): L.DivIcon {
  return L.divIcon({
    className: "",
    html: `<div class="mk ${cls}"></div>`,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  });
}

export const fromIcon = dot("mk-from", 16);
export const stopIcon = dot("mk-stop", 13);

export const pinIcon = L.divIcon({
  className: "",
  iconSize: [30, 40],
  iconAnchor: [15, 40],
  html:
    '<div class="pin"><svg width="30" height="40" viewBox="0 0 30 40">' +
    '<path d="M15 0C6.7 0 0 6.7 0 15c0 10 15 25 15 25s15-15 15-25C30 6.7 23.3 0 15 0Z" fill="#F5333F"/>' +
    '<circle cx="15" cy="15" r="6" fill="#fff"/></svg></div>',
});
