import random
import string
from io import BytesIO
from PIL import Image, ImageDraw, ImageFont
from django.core.cache import cache
from django.utils import timezone


def generate_random_string(length=4):
    """生成随机字符串"""
    chars = string.ascii_uppercase + string.digits
    return ''.join(random.choice(chars) for _ in range(length))


def generate_captcha_image(text, width=160, height=60):
    """生成验证码图片"""
    image = Image.new('RGB', (width, height), color=(255, 255, 255))
    draw = ImageDraw.Draw(image)
    
    try:
        font = ImageFont.truetype('arial.ttf', 36)
    except:
        font = ImageFont.load_default()
    
    for i, char in enumerate(text):
        x = 20 + i * 35
        y = random.randint(10, 20)
        color = (random.randint(0, 100), random.randint(0, 100), random.randint(0, 100))
        draw.text((x, y), char, font=font, fill=color)
    
    for _ in range(5):
        x1 = random.randint(0, width)
        y1 = random.randint(0, height)
        x2 = random.randint(0, width)
        y2 = random.randint(0, height)
        color = (random.randint(150, 200), random.randint(150, 200), random.randint(150, 200))
        draw.line([(x1, y1), (x2, y2)], fill=color, width=1)
    
    for _ in range(50):
        x = random.randint(0, width)
        y = random.randint(0, height)
        color = (random.randint(0, 255), random.randint(0, 255), random.randint(0, 255))
        draw.point((x, y), fill=color)
    
    buffer = BytesIO()
    image.save(buffer, format='PNG')
    buffer.seek(0)
    return buffer


def create_captcha(captcha_id=None):
    """创建验证码"""
    if not captcha_id:
        captcha_id = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(32))
    
    text = generate_random_string()
    image = generate_captcha_image(text)
    
    cache_key = f'captcha:{captcha_id}'
    cache.set(cache_key, text.lower(), timeout=300)
    
    return captcha_id, image


def verify_captcha(captcha_id, user_input):
    """验证验证码"""
    cache_key = f'captcha:{captcha_id}'
    stored_text = cache.get(cache_key)
    
    if stored_text and stored_text == user_input.lower():
        cache.delete(cache_key)
        return True
    
    return False


class SliderCaptcha:
    """滑块验证码"""
    
    def __init__(self, width=300, height=150, block_size=50):
        self.width = width
        self.height = height
        self.block_size = block_size
        self.target_x = random.randint(block_size, width - block_size - 50)
        self.target_y = random.randint(block_size, height - block_size - 50)
    
    def generate(self):
        """生成滑块验证码"""
        from PIL import Image, ImageDraw
        
        image = Image.new('RGB', (self.width, self.height), color=(200, 200, 200))
        draw = ImageDraw.Draw(image)
        
        for i in range(10):
            x = random.randint(0, self.width)
            y = random.randint(0, self.height)
            color = (random.randint(100, 255), random.randint(100, 255), random.randint(100, 255))
            draw.rectangle([x, y, x + 30, y + 30], fill=color)
        
        block = Image.new('RGBA', (self.block_size, self.block_size), (0, 0, 0, 0))
        block_draw = ImageDraw.Draw(block)
        block_draw.rectangle([0, 0, self.block_size, self.block_size], fill=(255, 255, 255, 200))
        block_draw.rectangle([5, 5, self.block_size - 5, self.block_size - 5], fill=(100, 100, 255, 150))
        
        image.paste(block, (self.target_x, self.target_y), block)
        
        buffer = BytesIO()
        image.save(buffer, format='PNG')
        buffer.seek(0)
        
        return {
            'image': buffer,
            'target_x': self.target_x,
            'target_y': self.target_y,
            'block_size': self.block_size
        }
    
    @staticmethod
    def verify(target_x, user_x, tolerance=5):
        """验证滑块验证码"""
        return abs(target_x - user_x) <= tolerance


def create_slider_captcha(captcha_id=None):
    """创建滑块验证码"""
    if not captcha_id:
        captcha_id = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(32))
    
    slider = SliderCaptcha()
    data = slider.generate()
    
    cache_key = f'slider_captcha:{captcha_id}'
    cache.set(cache_key, data['target_x'], timeout=300)
    
    return captcha_id, data


def verify_slider_captcha(captcha_id, user_x):
    """验证滑块验证码"""
    cache_key = f'slider_captcha:{captcha_id}'
    target_x = cache.get(cache_key)
    
    if target_x is not None and SliderCaptcha.verify(target_x, user_x):
        cache.delete(cache_key)
        return True
    
    return False


class NoCaptcha:
    """无感验证（行为验证）"""
    
    @staticmethod
    def create_session():
        """创建验证会话"""
        session_id = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(32))
        cache.set(f'nocaptcha:{session_id}', {'verified': False, 'start_time': timezone.now()}, timeout=600)
        return session_id
    
    @staticmethod
    def verify(session_id, user_data=None):
        """验证"""
        cache_key = f'nocaptcha:{session_id}'
        session_data = cache.get(cache_key)
        
        if session_data:
            session_data['verified'] = True
            session_data['verified_at'] = timezone.now()
            if user_data:
                session_data['user_data'] = user_data
            cache.set(cache_key, session_data, timeout=300)
            return True
        
        return False
    
    @staticmethod
    def is_verified(session_id):
        """检查是否已验证"""
        cache_key = f'nocaptcha:{session_id}'
        session_data = cache.get(cache_key)
        return session_data and session_data.get('verified', False)
