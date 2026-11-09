// Basic building blocks styled from the shared theme. Screens use these instead of raw
// Text / Pressable / TextInput so every screen looks the same.
import { toneFor, type Tone, type TypographyVariant } from '@qvorum/shared';
import type { ReactNode } from 'react';
import {
  ActivityIndicator,
  Pressable,
  Text,
  TextInput,
  View,
  type TextInputProps,
  type TextProps,
  type ViewProps,
} from 'react-native';
import { makeStyles, useTheme } from '@/lib/theme';

type TextColor = 'text' | 'textMuted' | 'textSubtle' | 'primary' | 'danger' | 'success';

export function AppText({
  variant = 'body',
  color = 'text',
  style,
  ...props
}: TextProps & { variant?: TypographyVariant; color?: TextColor }) {
  const theme = useTheme();
  return <Text {...props} style={[theme.typography[variant], { color: theme.colors[color] }, style]} />;
}

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';

export function Button({
  title,
  onPress,
  variant = 'primary',
  disabled,
  loading,
}: {
  title: string;
  onPress: () => void;
  variant?: ButtonVariant;
  disabled?: boolean;
  loading?: boolean;
}) {
  const theme = useTheme();
  const styles = useStyles();
  const { colors } = theme;
  const look = {
    primary: { bg: colors.primary, pressed: colors.primaryPressed, fg: colors.onPrimary, border: colors.primary },
    secondary: { bg: colors.surface, pressed: colors.surfaceMuted, fg: colors.text, border: colors.borderStrong },
    ghost: { bg: 'transparent', pressed: theme.tones.brand.bg, fg: colors.primary, border: 'transparent' },
    danger: { bg: colors.danger, pressed: colors.danger, fg: colors.surface, border: colors.danger },
  }[variant];
  const inactive = disabled || loading;
  return (
    <Pressable
      accessibilityRole="button"
      disabled={inactive}
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        { backgroundColor: pressed ? look.pressed : look.bg, borderColor: look.border, opacity: inactive ? 0.55 : 1 },
      ]}>
      {loading ? (
        <ActivityIndicator color={look.fg} />
      ) : (
        <Text style={[theme.typography.bodyStrong, { color: look.fg }]}>{title}</Text>
      )}
    </Pressable>
  );
}

export function Card({ style, ...props }: ViewProps) {
  const styles = useStyles();
  return <View {...props} style={[styles.card, style]} />;
}

export function Field({ label, error, ...props }: TextInputProps & { label: string; error?: string | null }) {
  const theme = useTheme();
  const styles = useStyles();
  return (
    <View style={styles.field}>
      <AppText variant="label">{label}</AppText>
      <TextInput
        placeholderTextColor={theme.colors.textSubtle}
        {...props}
        style={[styles.input, error ? { borderColor: theme.colors.danger } : null, props.style]}
      />
      {error ? <AppText variant="caption" color="danger">{error}</AppText> : null}
    </View>
  );
}

export function Badge({ tone = 'neutral', children }: { tone?: Tone; children: ReactNode }) {
  const theme = useTheme();
  const styles = useStyles();
  const colors = theme.tones[tone];
  return (
    <View style={[styles.badge, { backgroundColor: colors.bg }]}>
      <Text style={[theme.typography.caption, { color: colors.fg, fontWeight: theme.fontWeight.semibold }]}>{children}</Text>
    </View>
  );
}

/** Badge for a status value (NEW, PAID...), colored by the shared status tones. */
export function StatusBadge({ status, label }: { status: string; label?: string }) {
  return <Badge tone={toneFor(status)}>{label ?? status.replace(/_/g, ' ').toLowerCase()}</Badge>;
}

const useStyles = makeStyles((t) => ({
  button: {
    minHeight: t.controlHeight.md,
    paddingHorizontal: t.spacing.lg,
    borderRadius: t.radius.md,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  card: {
    backgroundColor: t.colors.surface,
    borderColor: t.colors.border,
    borderWidth: 1,
    borderRadius: t.radius.lg,
    padding: t.spacing.lg,
    gap: t.spacing.md,
    ...t.shadow.sm.native,
  },
  field: {
    gap: t.spacing.xs,
  },
  input: {
    minHeight: t.controlHeight.md,
    paddingHorizontal: t.spacing.md,
    borderWidth: 1,
    borderColor: t.colors.borderStrong,
    borderRadius: t.radius.md,
    backgroundColor: t.colors.surface,
    color: t.colors.text,
    fontSize: t.fontSize.md,
  },
  badge: {
    alignSelf: 'flex-start',
    paddingHorizontal: t.spacing.sm,
    paddingVertical: t.spacing.xxs,
    borderRadius: t.radius.full,
  },
}));
