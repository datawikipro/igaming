import React, { useState } from 'react';

export interface PlayerAvatarProps {
  playerName: string;
  avatarUrl?: string | null;
  flagUrl?: string | null;
  countryCode?: string | null;
  country?: string | null;
  ranking?: number | null;
  seedNumber?: number | null;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  isTopSeed?: boolean;
  className?: string;
  onClick?: () => void;
}

const sizeConfig = {
  sm: {
    container: 'w-10 h-10',
    avatar: 'w-10 h-10',
    flag: 'w-3.5 h-3.5 bottom-0 right-0',
    seed: 'text-[9px] px-1 py-0.5 -top-1.5 -left-1.5',
    initials: 'text-xs',
    ring: 'ring-2',
  },
  md: {
    container: 'w-14 h-14',
    avatar: 'w-14 h-14',
    flag: 'w-5 h-5 bottom-0 right-0',
    seed: 'text-[10px] px-1.5 py-0.5 -top-2 -left-2',
    initials: 'text-sm font-bold',
    ring: 'ring-2',
  },
  lg: {
    container: 'w-20 h-20',
    avatar: 'w-20 h-20',
    flag: 'w-6 h-6 bottom-0.5 right-0.5',
    seed: 'text-xs px-2 py-0.5 -top-2 -left-2 font-mono font-bold',
    initials: 'text-lg font-extrabold',
    ring: 'ring-2',
  },
  xl: {
    container: 'w-28 h-28',
    avatar: 'w-28 h-28',
    flag: 'w-8 h-8 bottom-1 right-1',
    seed: 'text-sm px-2.5 py-1 -top-2.5 -left-2.5 font-mono font-black',
    initials: 'text-2xl font-black',
    ring: 'ring-3',
  },
};

export const PlayerAvatar: React.FC<PlayerAvatarProps> = ({
  playerName,
  avatarUrl,
  flagUrl,
  countryCode,
  country,
  ranking,
  seedNumber,
  size = 'md',
  isTopSeed,
  className = '',
  onClick,
}) => {
  const [imageError, setImageError] = useState(false);
  const cfg = sizeConfig[size] || sizeConfig.md;

  const topSeedCheck =
    isTopSeed !== undefined
      ? isTopSeed
      : (seedNumber !== null && seedNumber !== undefined && seedNumber <= 5) ||
        (ranking !== null && ranking !== undefined && ranking <= 5);

  const ringStyle = topSeedCheck
    ? 'ring-[#FFD700] shadow-[0_0_15px_rgba(255,215,0,0.5)] border-amber-400'
    : 'ring-[#00F0FF] shadow-[0_0_15px_rgba(0,240,255,0.4)] border-cyan-400';

  const extractInitials = (name: string): string => {
    if (!name) return 'SB';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  };

  const cleanSurname = (name: string): string => {
    if (!name) return '';
    const parts = name.trim().split(/\s+/);
    return parts[parts.length - 1];
  };

  const effectiveAvatar =
    !imageError && avatarUrl && avatarUrl.trim() !== ''
      ? avatarUrl
      : `/cdn/avatars/svg/${encodeURIComponent(
          playerName.toLowerCase().replace(/[^a-z0-9]/g, '-')
        )}?seed=${seedNumber || ''}`;

  return (
    <div
      className={`relative inline-flex items-center justify-center select-none group cursor-pointer ${cfg.container} ${className}`}
      onClick={onClick}
      title={`${playerName}${ranking ? ` (ATP/WTA #${ranking})` : ''}${
        country ? ` - ${country}` : ''
      }`}
    >
      {/* Seed Badge (Top-Left) */}
      {seedNumber !== null && seedNumber !== undefined && seedNumber > 0 && (
        <span
          className={`absolute z-20 rounded bg-[#0a0f1d] border border-cyan-500/50 text-cyan-300 font-mono shadow-md ${cfg.seed} transition-transform group-hover:scale-110`}
          style={
            topSeedCheck
              ? { borderColor: '#FFD700', color: '#FFD700', boxShadow: '0 0 8px rgba(255,215,0,0.6)' }
              : {}
          }
        >
          [{seedNumber}]
        </span>
      )}

      {/* Main Avatar Container */}
      <div
        className={`relative overflow-hidden rounded-full bg-gradient-to-br from-slate-900 via-slate-800 to-black ${cfg.avatar} ${cfg.ring} ${ringStyle} transition-all duration-300 group-hover:scale-105 flex items-center justify-center`}
      >
        {!imageError ? (
          <img
            src={effectiveAvatar}
            alt={playerName}
            className="w-full h-full object-cover object-top transition-opacity duration-300"
            onError={() => setImageError(true)}
            loading="lazy"
          />
        ) : (
          <div className="w-full h-full flex flex-col items-center justify-center bg-gradient-to-b from-[#0e1628] to-[#080d19] text-white">
            <span
              className={`tracking-wider ${cfg.initials} bg-gradient-to-r from-cyan-300 to-blue-400 bg-clip-text text-transparent`}
            >
              {extractInitials(playerName)}
            </span>
            {size !== 'sm' && (
              <span className="text-[9px] font-semibold text-slate-400 uppercase tracking-tighter truncate max-w-[80%]">
                {cleanSurname(playerName)}
              </span>
            )}
          </div>
        )}
      </div>

      {/* Country Flag Badge (Bottom-Right) */}
      {(flagUrl || countryCode) && (
        <div
          className={`absolute z-20 rounded-full overflow-hidden border border-slate-700 bg-slate-950 shadow-md ${cfg.flag} transition-transform group-hover:scale-110`}
          title={country || countryCode || 'Country'}
        >
          <img
            src={
              flagUrl ||
              `https://flagcdn.com/w40/${countryCode ? countryCode.toLowerCase() : 'un'}.png`
            }
            alt={countryCode || 'Flag'}
            className="w-full h-full object-cover"
            onError={(e) => {
              (e.target as HTMLElement).style.display = 'none';
            }}
          />
        </div>
      )}
    </div>
  );
};

export default PlayerAvatar;
