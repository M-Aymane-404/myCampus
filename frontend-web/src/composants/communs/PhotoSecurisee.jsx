import { useMemo } from 'react';

import { useSecuredObjectUrl } from '../../utils/useSecuredObjectUrl';

const defaultFallback = null;

const PhotoSecurisee = ({
  photoUrl,
  alt = 'Photo',
  className,
  style,
  fallback = defaultFallback,
}) => {
  const trimmed = useMemo(() => (photoUrl ? String(photoUrl).trim() : ''), [photoUrl]);
  const { url, loading } = useSecuredObjectUrl(trimmed, { enabled: !!trimmed });

  if (!trimmed) return fallback;
  if (!url) return fallback;

  return (
    <img
      src={url}
      alt={alt}
      className={className}
      style={style}
      draggable="false"
      aria-busy={loading ? 'true' : 'false'}
    />
  );
};

export default PhotoSecurisee;

