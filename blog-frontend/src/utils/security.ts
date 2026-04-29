export class SecurityUtils {
  private static readonly XSS_PATTERNS = [
    /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
    /javascript:/gi,
    /on\w+=/gi,
    /<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi,
    /<form\b[^<]*(?:(?!<\/form>)<[^<]*)*<\/form>/gi,
  ];

  static sanitizeHTML(input: string): string {
    if (!input) return input;
    
    let sanitized = input;
    this.XSS_PATTERNS.forEach((pattern) => {
      sanitized = sanitized.replace(pattern, "");
    });
    
    return sanitized;
  }

  static escapeHTML(input: string): string {
    if (!input) return input;
    
    const div = document.createElement("div");
    div.textContent = input;
    return div.innerHTML;
  }

  static validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  static validatePassword(password: string): {
    valid: boolean;
    message: string;
  } {
    if (password.length < 8) {
      return { valid: false, message: "密码至少需要8个字符" };
    }
    
    if (!/[A-Z]/.test(password)) {
      return { valid: false, message: "密码需要包含大写字母" };
    }
    
    if (!/[a-z]/.test(password)) {
      return { valid: false, message: "密码需要包含小写字母" };
    }
    
    if (!/[0-9]/.test(password)) {
      return { valid: false, message: "密码需要包含数字" };
    }
    
    return { valid: true, message: "密码强度足够" };
  }

  static validateUsername(username: string): {
    valid: boolean;
    message: string;
  } {
    if (username.length < 3 || username.length > 20) {
      return { valid: false, message: "用户名长度应在3-20个字符之间" };
    }
    
    const usernameRegex = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/;
    if (!usernameRegex.test(username)) {
      return { valid: false, message: "用户名只能包含字母、数字、下划线和中文" };
    }
    
    return { valid: true, message: "用户名格式正确" };
  }

  static generateCSRFToken(): string {
    const array = new Uint8Array(32);
    crypto.getRandomValues(array);
    return Array.from(array, (byte) =>
      byte.toString(16).padStart(2, "0")
    ).join("");
  }

  static checkRateLimit(key: string, maxAttempts: number = 5, windowMs: number = 60000): boolean {
    const now = Date.now();
    const windowStart = now - windowMs;
    
    const keyName = `rate_limit_${key}`;
    let attempts = JSON.parse(localStorage.getItem(keyName) || "[]");
    attempts = attempts.filter((time: number) => time > windowStart);
    
    if (attempts.length >= maxAttempts) {
      return false;
    }
    
    attempts.push(now);
    localStorage.setItem(keyName, JSON.stringify(attempts));
    return true;
  }

  static maskEmail(email: string): string {
    if (!email) return email;
    const [name, domain] = email.split("@");
    if (!name || !domain) return email;
    
    const maskedName = name.length > 2 
      ? name.charAt(0) + "*".repeat(name.length - 2) + name.charAt(name.length - 1)
      : name.charAt(0) + "*";
    
    return `${maskedName}@${domain}`;
  }

  static maskPhone(phone: string): string {
    if (!phone || phone.length < 7) return phone;
    return phone.slice(0, 3) + "*".repeat(phone.length - 7) + phone.slice(-4);
  }

  static sanitizeURL(url: string): string | null {
    try {
      const parsedUrl = new URL(url);
      if (!["http:", "https:"].includes(parsedUrl.protocol)) {
        return null;
      }
      return parsedUrl.toString();
    } catch {
      return null;
    }
  }

  static preventClickJacking(): void {
    if (window.top && window.self !== window.top) {
      window.top.location.href = window.self.location.href;
    }
  }
}

export default SecurityUtils;
