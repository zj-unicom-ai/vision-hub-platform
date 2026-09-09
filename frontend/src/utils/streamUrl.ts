
export function isRtspUrl(url: string) {
  return /^rtsp:/i.test(url.trim())
}
