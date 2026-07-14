export const screenBackgrounds = {
  welcome: {
    hero: 'radial-gradient(circle at 35% 72%, #8e226f 0 12%, transparent 29%), radial-gradient(circle at 84% 58%, #ff8a8a 0 20%, transparent 48%), linear-gradient(126deg, #ff224f 0%, #ff4662 42%, #ff8a91 100%)',
    body: 'radial-gradient(ellipse at 18% 18%, #d9dadd 0 7%, transparent 7.4%), radial-gradient(ellipse at 34% 28%, #d9dadd 0 10%, transparent 10.4%), radial-gradient(ellipse at 51% 30%, #d9dadd 0 8%, transparent 8.4%), radial-gradient(ellipse at 70% 24%, #d9dadd 0 12%, transparent 12.4%), radial-gradient(ellipse at 79% 47%, #d9dadd 0 9%, transparent 9.4%), radial-gradient(ellipse at 54% 55%, #d9dadd 0 13%, transparent 13.4%), radial-gradient(ellipse at 30% 57%, #d9dadd 0 11%, transparent 11.4%), radial-gradient(ellipse at 45% 80%, #d9dadd 0 8%, transparent 8.4%)'
  },
  gatePass: {
    hero: 'radial-gradient(circle at 35% 72%, #8e226f 0 12%, transparent 29%), radial-gradient(circle at 84% 58%, #ff8a8a 0 20%, transparent 48%), linear-gradient(126deg, #ff224f 0%, #ff4662 42%, #ff8a91 100%)',
    body: 'radial-gradient(ellipse at 18% 18%, #d9dadd 0 7%, transparent 7.4%), radial-gradient(ellipse at 34% 28%, #d9dadd 0 10%, transparent 10.4%), radial-gradient(ellipse at 51% 30%, #d9dadd 0 8%, transparent 8.4%), radial-gradient(ellipse at 70% 24%, #d9dadd 0 12%, transparent 12.4%), radial-gradient(ellipse at 79% 47%, #d9dadd 0 9%, transparent 9.4%), radial-gradient(ellipse at 54% 55%, #d9dadd 0 13%, transparent 13.4%), radial-gradient(ellipse at 30% 57%, #d9dadd 0 11%, transparent 11.4%), radial-gradient(ellipse at 45% 80%, #d9dadd 0 8%, transparent 8.4%)'
  },
  tasreeh: {
    hero: 'radial-gradient(circle at 35% 72%, #8e226f 0 12%, transparent 29%), radial-gradient(circle at 84% 58%, #ff8a8a 0 20%, transparent 48%), linear-gradient(126deg, #ff224f 0%, #ff4662 42%, #ff8a91 100%)',
    body: 'radial-gradient(ellipse at 18% 18%, #d9dadd 0 7%, transparent 7.4%), radial-gradient(ellipse at 34% 28%, #d9dadd 0 10%, transparent 10.4%), radial-gradient(ellipse at 51% 30%, #d9dadd 0 8%, transparent 8.4%), radial-gradient(ellipse at 70% 24%, #d9dadd 0 12%, transparent 12.4%), radial-gradient(ellipse at 79% 47%, #d9dadd 0 9%, transparent 9.4%), radial-gradient(ellipse at 54% 55%, #d9dadd 0 13%, transparent 13.4%), radial-gradient(ellipse at 30% 57%, #d9dadd 0 11%, transparent 11.4%), radial-gradient(ellipse at 45% 80%, #d9dadd 0 8%, transparent 8.4%)'
  }
} as const;

export type ScreenBackgroundKey = keyof typeof screenBackgrounds;

export function screenBackgroundStyle(screen: ScreenBackgroundKey) {
  const background = screenBackgrounds[screen];
  return {
    '--screen-hero-background': background.hero,
    '--screen-body-background': background.body
  } as Record<string, string>;
}
