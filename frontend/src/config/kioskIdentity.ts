const KIOSK_ID_STORAGE_KEY = 'ssk-kiosk-id';
const KIOSK_ID_PATTERN = /^[A-Za-z0-9._-]{1,64}$/;

type IdentitySources = {
  search: string;
  configuredIdentity?: string;
  storedIdentity?: string | null;
  randomUUID: () => string;
};

function validIdentity(value?: string | null): string | undefined {
  const normalized = value?.trim();
  return normalized && KIOSK_ID_PATTERN.test(normalized) ? normalized : undefined;
}

/** Resolve a stable identity, preferring per-device provisioning over build defaults. */
export function resolveKioskIdentity(sources: IdentitySources): string {
  const queryIdentity = validIdentity(new URLSearchParams(sources.search).get('kioskId'));
  return queryIdentity
    ?? validIdentity(sources.storedIdentity)
    ?? validIdentity(sources.configuredIdentity)
    ?? `WEB-${sources.randomUUID()}`;
}

let cachedIdentity: string | undefined;

export function getKioskIdentity(): string {
  if (cachedIdentity) return cachedIdentity;

  let storedIdentity: string | null = null;
  try { storedIdentity = localStorage.getItem(KIOSK_ID_STORAGE_KEY); } catch { /* storage may be disabled */ }

  cachedIdentity = resolveKioskIdentity({
    search: window.location.search,
    configuredIdentity: import.meta.env.VITE_KIOSK_ID,
    storedIdentity,
    randomUUID: () => crypto.randomUUID(),
  });

  try { localStorage.setItem(KIOSK_ID_STORAGE_KEY, cachedIdentity); } catch { /* use in-memory identity */ }
  return cachedIdentity;
}
