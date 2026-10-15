import { useState, useRef, useEffect, useCallback } from "react";
import { Mail, ShieldCheck, Lock, CheckCircle, ArrowLeft, Loader2, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { customInstance } from "@/api/custom-instance";
import { extractErrorMessage } from "@/lib/extract-error";
import { toast } from "sonner";
import logo from "@/assets/logo.png";

interface Props {
  onBackToLogin: () => void;
}

type Step = "email" | "code" | "password" | "success";

const COOLDOWN_SECONDS = 30;

const inputClasses = `w-full
  rounded-md
  bg-white/80 backdrop-blur-sm
  border border-rose-800
  text-gray-800 placeholder-gray-400
  transition-all duration-300 ease-in-out
  focus:outline-none
  focus:ring-2 focus:ring-rose-500/60
  focus:border-rose-800
  hover:border-rose-600
  shadow-sm hover:shadow-md focus:shadow-lg
`;

export default function RecoverPasswordCard({ onBackToLogin }: Props) {
  const [step, setStep] = useState<Step>("email");
  const [email, setEmail] = useState("");
  const [code, setCode] = useState(["", "", "", "", "", ""]);
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [cooldown, setCooldown] = useState(0);
  const [sending, setSending] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [resetting, setResetting] = useState(false);
  const [error, setError] = useState("");
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const codeInputsRef = useRef<(HTMLInputElement | null)[]>([]);

  const clearTimer = useCallback(() => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  useEffect(() => {
    return () => clearTimer();
  }, [clearTimer]);

  const startCooldown = () => {
    clearTimer();
    setCooldown(COOLDOWN_SECONDS);
    timerRef.current = setInterval(() => {
      setCooldown((prev) => {
        if (prev <= 1) {
          clearTimer();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const resetAll = () => {
    clearTimer();
    setStep("email");
    setEmail("");
    setCode(["", "", "", "", "", ""]);
    setPassword("");
    setConfirmPassword("");
    setCooldown(0);
    setError("");
  };

  const handleSendCode = async () => {
    if (!email.trim()) return;
    setError("");
    setSending(true);
    try {
      await customInstance({ url: "/api/auth/forgot-password", method: "POST", data: { email } });
      setStep("code");
      startCooldown();
      setTimeout(() => codeInputsRef.current[0]?.focus(), 100);
      toast.success("Código enviado a tu correo");
    } catch (err: unknown) {
      setError(extractErrorMessage(err, "Error al enviar el código"));
    } finally {
      setSending(false);
    }
  };

  const handleVerifyCode = async () => {
    if (code.some((d) => !d)) return;
    setError("");
    setVerifying(true);
    try {
      const codigo = code.join("");
      await customInstance({ url: "/api/auth/verify-reset-code", method: "POST", data: { email, codigo } });
      setStep("password");
    } catch (err: unknown) {
      setError(extractErrorMessage(err, "Código inválido"));
    } finally {
      setVerifying(false);
    }
  };

  const handleResetPassword = async () => {
    if (!password || password !== confirmPassword) return;
    setError("");
    setResetting(true);
    try {
      const codigo = code.join("");
      await customInstance({ url: "/api/auth/reset-password", method: "POST", data: { email, codigo, password } });
      setStep("success");
      toast.success("Contraseña restablecida correctamente");
    } catch (err: unknown) {
      setError(extractErrorMessage(err, "Error al restablecer la contraseña"));
    } finally {
      setResetting(false);
    }
  };

  const handleResend = async () => {
    setCode(["", "", "", "", "", ""]);
    setError("");
    setSending(true);
    try {
      await customInstance({ url: "/api/auth/forgot-password", method: "POST", data: { email } });
      startCooldown();
      setTimeout(() => codeInputsRef.current[0]?.focus(), 100);
      toast.success("Código reenviado");
    } catch (err: unknown) {
      setError(extractErrorMessage(err, "Error al reenviar el código"));
    } finally {
      setSending(false);
    }
  };

  const handleCodeChange = (index: number, value: string) => {
    if (!/^\d?$/.test(value)) return;
    const next = [...code];
    next[index] = value;
    setCode(next);
    if (value && index < 5) {
      codeInputsRef.current[index + 1]?.focus();
    }
  };

  const handleCodeKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      const next = [...code];
      next[index - 1] = "";
      setCode(next);
      codeInputsRef.current[index - 1]?.focus();
    }
  };

  const handleCodePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const data = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
    const next = [...code];
    for (let i = 0; i < data.length; i++) {
      next[i] = data[i];
    }
    setCode(next);
    const focusIndex = Math.min(data.length, 5);
    codeInputsRef.current[focusIndex]?.focus();
  };

  const goBack = () => {
    setError("");
    if (step === "code") {
      clearTimer();
      setCode(["", "", "", "", "", ""]);
      setCooldown(0);
      setStep("email");
    } else if (step === "password") {
      setPassword("");
      setConfirmPassword("");
      setStep("code");
      setTimeout(() => codeInputsRef.current[0]?.focus(), 100);
    }
  };

  return (
    <div className="w-full flex justify-center items-center p-4">
      <div className="bg-card shadow-2xl rounded-xl p-8 w-full max-w-md flex flex-col items-center relative">
        {step !== "email" && (
          <button
            type="button"
            onClick={goBack}
            className="absolute left-4 top-4 cursor-pointer text-stone-400 hover:text-stone-600 transition-colors bg-transparent border-none"
          >
            <ArrowLeft className="size-5" />
          </button>
        )}

        <img src={logo} alt="logo" className="w-45 h-auto rounded-xl" />
        <h1 className="text-2xl font-heading font-semibold text-gray-800 dark:text-rose-800 mb-1">
          Hotel Cacique de Bolivar
        </h1>

        {error && (
          <div className="w-full mt-3 flex items-center gap-2 rounded-lg bg-red-50 p-3 text-sm text-red-700">
            <AlertCircle className="size-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {step === "email" && (
          <StepEmail
            email={email}
            onEmailChange={setEmail}
            onContinue={handleSendCode}
            onBack={onBackToLogin}
            sending={sending}
          />
        )}

        {step === "code" && (
          <StepCode
            email={email}
            code={code}
            onCodeChange={handleCodeChange}
            onCodeKeyDown={handleCodeKeyDown}
            onCodePaste={handleCodePaste}
            inputRefs={codeInputsRef}
            onContinue={handleVerifyCode}
            verifying={verifying}
            cooldown={cooldown}
            onResend={handleResend}
            sending={sending}
          />
        )}

        {step === "password" && (
          <StepPassword
            password={password}
            confirmPassword={confirmPassword}
            onPasswordChange={setPassword}
            onConfirmChange={setConfirmPassword}
            onContinue={handleResetPassword}
            resetting={resetting}
          />
        )}

        {step === "success" && (
          <StepSuccess onContinue={() => { resetAll(); onBackToLogin(); }} />
        )}
      </div>
    </div>
  );
}

/* ── Step 1: Email ── */
function StepEmail({
  email, onEmailChange, onContinue, onBack, sending,
}: {
  email: string; onEmailChange: (v: string) => void; onContinue: () => void; onBack: () => void; sending: boolean;
}) {
  return (
    <div className="flex flex-col items-center gap-5 w-full mt-4">
      <div className="flex size-14 items-center justify-center rounded-full bg-rose-50">
        <Mail className="size-7 text-rose-500" />
      </div>

      <div className="text-center">
        <h2 className="font-heading text-lg font-semibold text-gray-800">
          Recuperar contraseña
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          Ingresa tu correo electrónico registrado
        </p>
      </div>

      <div className="w-full space-y-1">
        <label className="text-sm font-medium text-gray-700 dark:text-rose-800">
          Correo electrónico
        </label>
        <Input
          type="email"
          placeholder="correo@ejemplo.com"
          value={email}
          onChange={(e) => onEmailChange(e.target.value)}
          className={inputClasses}
        />
      </div>

      <Button
        onClick={onContinue}
        disabled={!email.trim() || sending}
        className="w-full h-11 bg-rose-800 hover:bg-rose-700 text-white font-semibold rounded-xl shadow-md hover:shadow-lg transition-all duration-200 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed"
      >
        {sending ? <Loader2 className="size-4 animate-spin" /> : null}
        {sending ? "Enviando..." : "Enviar código de verificación"}
      </Button>

      <button
        type="button"
        onClick={onBack}
        className="text-sm font-medium text-rose-600 hover:underline cursor-pointer bg-transparent border-none"
      >
        Volver al inicio de sesión
      </button>
    </div>
  );
}

/* ── Step 2: Code ── */
function StepCode({
  email, code, onCodeChange, onCodeKeyDown, onCodePaste, inputRefs, onContinue, verifying, cooldown, onResend, sending,
}: {
  email: string; code: string[]; onCodeChange: (i: number, v: string) => void;
  onCodeKeyDown: (i: number, e: React.KeyboardEvent) => void; onCodePaste: (e: React.ClipboardEvent) => void;
  inputRefs: React.MutableRefObject<(HTMLInputElement | null)[]>;
  onContinue: () => void; verifying: boolean; cooldown: number; onResend: () => void; sending: boolean;
}) {
  return (
    <div className="flex flex-col items-center gap-5 w-full mt-4">
      <div className="flex size-14 items-center justify-center rounded-full bg-rose-50">
        <ShieldCheck className="size-7 text-rose-500" />
      </div>

      <div className="text-center">
        <h2 className="font-heading text-lg font-semibold text-gray-800">
          Código de verificación
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          Te enviamos un código a{" "}
          <strong className="text-gray-700">{email}</strong>
        </p>
      </div>

      <div className="w-full space-y-2">
        <label className="text-sm font-medium text-gray-700 dark:text-rose-800 block text-center">
          Código de 6 dígitos
        </label>
        <div className="flex items-center justify-center gap-2" onPaste={onCodePaste}>
          {code.map((digit, i) => (
            <input
              key={i}
              ref={(el) => { inputRefs.current[i] = el; }}
              type="text"
              inputMode="numeric"
              autoComplete={i === 0 ? "one-time-code" : "off"}
              maxLength={1}
              value={digit}
              onChange={(e) => onCodeChange(i, e.target.value)}
              onKeyDown={(e) => onCodeKeyDown(i, e)}
              className={`w-11 h-12 text-center text-xl font-mono font-bold rounded-lg border transition-all duration-150 outline-none
                ${digit ? "border-rose-800 bg-white shadow-sm ring-2 ring-rose-500/30" : "border-stone-300 bg-white/80"}
                focus:border-rose-800 focus:ring-2 focus:ring-rose-500/60`}
            />
          ))}
        </div>
      </div>

      <Button
        onClick={onContinue}
        disabled={code.some((d) => !d) || verifying}
        className="w-full h-11 bg-rose-800 hover:bg-rose-700 text-white font-semibold rounded-xl shadow-md hover:shadow-lg transition-all duration-200 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed"
      >
        {verifying ? <Loader2 className="size-4 animate-spin" /> : null}
        {verifying ? "Verificando..." : "Verificar código"}
      </Button>

      <div className="flex items-center gap-1 text-sm">
        <span className="text-gray-400">¿No recibiste el código?</span>
        {cooldown > 0 ? (
          <span className="text-gray-400 font-medium">Reenviar en {cooldown}s</span>
        ) : (
          <button
            type="button"
            onClick={onResend}
            disabled={sending}
            className="text-rose-600 hover:underline cursor-pointer bg-transparent border-none font-medium disabled:opacity-50"
          >
            {sending ? "Enviando..." : "Reenviar código"}
          </button>
        )}
      </div>
    </div>
  );
}

/* ── Step 3: Password ── */
function StepPassword({
  password, confirmPassword, onPasswordChange, onConfirmChange, onContinue, resetting,
}: {
  password: string; confirmPassword: string; onPasswordChange: (v: string) => void;
  onConfirmChange: (v: string) => void; onContinue: () => void; resetting: boolean;
}) {
  const match = password && confirmPassword && password === confirmPassword;
  const valid = password.length >= 6 && match;

  return (
    <div className="flex flex-col items-center gap-5 w-full mt-4">
      <div className="flex size-14 items-center justify-center rounded-full bg-rose-50">
        <Lock className="size-7 text-rose-500" />
      </div>

      <div className="text-center">
        <h2 className="font-heading text-lg font-semibold text-gray-800">
          Nueva contraseña
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          Ingresa tu nueva contraseña
        </p>
      </div>

      <div className="w-full space-y-4">
        <div className="space-y-1">
          <label className="text-sm font-medium text-gray-700 dark:text-rose-800">
            Nueva contraseña
          </label>
          <Input
            type="password"
            placeholder="Mínimo 6 caracteres"
            value={password}
            onChange={(e) => onPasswordChange(e.target.value)}
            className={inputClasses}
          />
        </div>

        <div className="space-y-1">
          <label className="text-sm font-medium text-gray-700 dark:text-rose-800">
            Confirmar contraseña
          </label>
          <Input
            type="password"
            placeholder="Repite la contraseña"
            value={confirmPassword}
            onChange={(e) => onConfirmChange(e.target.value)}
            className={inputClasses}
          />
          {confirmPassword && !match && (
            <p className="text-xs text-red-500 mt-1">Las contraseñas no coinciden</p>
          )}
        </div>
      </div>

      <Button
        onClick={onContinue}
        disabled={!valid || resetting}
        className="w-full h-11 bg-rose-800 hover:bg-rose-700 text-white font-semibold rounded-xl shadow-md hover:shadow-lg transition-all duration-200 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed"
      >
        {resetting ? <Loader2 className="size-4 animate-spin" /> : null}
        {resetting ? "Restableciendo..." : "Restablecer contraseña"}
      </Button>
    </div>
  );
}

/* ── Step 4: Success ── */
function StepSuccess({ onContinue }: { onContinue: () => void }) {
  return (
    <div className="flex flex-col items-center gap-5 w-full mt-4">
      <div className="flex size-14 items-center justify-center rounded-full bg-emerald-50">
        <CheckCircle className="size-7 text-emerald-500" />
      </div>

      <div className="text-center">
        <h2 className="font-heading text-lg font-semibold text-gray-800">
          Contraseña restablecida
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          Tu contraseña se ha actualizado correctamente.
        </p>
      </div>

      <Button
        onClick={onContinue}
        className="w-full h-11 bg-rose-800 hover:bg-rose-700 text-white font-semibold rounded-xl shadow-md hover:shadow-lg transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]"
      >
        Volver al inicio de sesión
      </Button>
    </div>
  );
}
