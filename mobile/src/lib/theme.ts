// Theme access for the mobile app: follows the phone's light / dark setting.
import {
  controlHeight,
  fontSize,
  fontWeight,
  radius,
  spacing,
  themes,
  typography,
  type Theme,
} from '@qvorum/shared';
import { useMemo } from 'react';
import { StyleSheet, useColorScheme, type ImageStyle, type TextStyle, type ViewStyle } from 'react-native';

export interface AppTheme extends Theme {
  spacing: typeof spacing;
  radius: typeof radius;
  fontSize: typeof fontSize;
  fontWeight: typeof fontWeight;
  typography: typeof typography;
  controlHeight: typeof controlHeight;
}

type NamedStyles<T> = { [K in keyof T]: ViewStyle | TextStyle | ImageStyle };

const build = (theme: Theme): AppTheme => ({ ...theme, spacing, radius, fontSize, fontWeight, typography, controlHeight });
const appThemes = { light: build(themes.light), dark: build(themes.dark) };

/** The current theme: colors, tones, shadows and all size tokens. */
export function useTheme(): AppTheme {
  return useColorScheme() === 'dark' ? appThemes.dark : appThemes.light;
}

/**
 * Declares styles that depend on the theme:
 *   const useStyles = makeStyles((t) => ({ box: { backgroundColor: t.colors.surface } }));
 *   const styles = useStyles();
 */
export function makeStyles<T extends NamedStyles<T>>(factory: (theme: AppTheme) => T) {
  return function useStyles(): T {
    const theme = useTheme();
    return useMemo(() => StyleSheet.create(factory(theme)), [theme]);
  };
}
