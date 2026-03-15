import 'package:flutter/material.dart';

class NotchBarItem {
  final IconData? icon;
  final Widget? customIcon;
  final String label;

  const NotchBarItem({this.icon, this.customIcon, required this.label})
    : assert(icon != null || customIcon != null);
}

class AnimatedNotchBottomBar extends StatelessWidget {
  const AnimatedNotchBottomBar({
    super.key,
    required this.items,
    required this.currentIndex,
    required this.onTap,
    this.height = 72,
    this.notchRadius = 28,
    this.notchSize = 52,
    this.activeColor = const Color(0xFF5B5BEA),
    this.inactiveColor = const Color(0xFF9AA3B2),
    this.backgroundColor = Colors.white,
    this.elevation = 16,
  }) : assert(items.length >= 2);

  final List<NotchBarItem> items;
  final int currentIndex;
  final ValueChanged<int> onTap;

  final double height;
  final double notchRadius;
  final double notchSize;
  final Color activeColor;
  final Color inactiveColor;
  final Color backgroundColor;
  final double elevation;

  double _centerXForIndex(double width, int index) {
    final step = width / items.length;
    return step * index + step / 2;
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      child: LayoutBuilder(
        builder: (context, c) {
          final w = c.maxWidth;
          final selectedX = _centerXForIndex(w, currentIndex);

          return SizedBox(
            height: height + 18,
            child: Stack(
              clipBehavior: Clip.none,
              children: [
                Positioned(
                  left: 0,
                  right: 0,
                  bottom: 0,
                  child: Material(
                    color: Colors.transparent,
                    elevation: elevation,
                    shadowColor: Colors.black.withOpacity(0.12),
                    child: ClipPath(
                      clipper: _NotchClipper(
                        notchCenterX: selectedX,
                        notchRadius: notchRadius + 10,
                      ),
                      child: Container(
                        height: height,
                        decoration: BoxDecoration(
                          color: backgroundColor,
                          border: Border(
                            top: BorderSide(
                              color: Colors.black.withOpacity(0.06),
                            ),
                          ),
                        ),
                        padding: const EdgeInsets.symmetric(horizontal: 8),
                        child: Row(
                          children: List.generate(items.length, (i) {
                            final isActive = i == currentIndex;
                            return Expanded(
                              child: InkWell(
                                onTap: () => onTap(i),
                                splashColor: activeColor.withOpacity(0.08),
                                highlightColor: Colors.transparent,
                                child: Padding(
                                  padding: const EdgeInsets.only(top: 6),
                                  child: Column(
                                    mainAxisSize: MainAxisSize.max,
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      const SizedBox(height: 12),
                                      if (!isActive)
                                        _InactiveIcon(
                                          item: items[i],
                                          inactiveColor: inactiveColor,
                                        )
                                      else
                                        const SizedBox(height: 22),
                                      const SizedBox(height: 4),
                                      Text(
                                        items[i].label,
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                        textScaler: const TextScaler.linear(1.0),
                                        style: TextStyle(
                                          fontSize: 11.5,
                                          fontWeight: FontWeight.w700,
                                          color: isActive ? activeColor : inactiveColor,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            );
                          }),
                        ),
                      ),
                    ),
                  ),
                ),
                AnimatedPositioned(
                  duration: const Duration(milliseconds: 320),
                  curve: Curves.easeOutCubic,
                  left: selectedX - (notchSize / 2),
                  bottom: height - (notchSize / 2) - 4,
                  child: _NotchButton(
                    size: notchSize,
                    color: activeColor,
                    item: items[currentIndex],
                    onTap: () => onTap(currentIndex),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _InactiveIcon extends StatelessWidget {
  const _InactiveIcon({required this.item, required this.inactiveColor});
  final NotchBarItem item;
  final Color inactiveColor;

  @override
  Widget build(BuildContext context) {
    if (item.customIcon != null) {
      return Opacity(
        opacity: 0.85,
        child: SizedBox(width: 22, height: 22, child: item.customIcon),
      );
    }
    return Icon(item.icon, size: 22, color: inactiveColor);
  }
}

class _NotchButton extends StatelessWidget {
  const _NotchButton({
    required this.size,
    required this.color,
    required this.item,
    required this.onTap,
  });

  final double size;
  final Color color;
  final NotchBarItem item;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.92, end: 1.0),
      duration: const Duration(milliseconds: 260),
      curve: Curves.easeOutBack,
      builder: (_, v, child) => Transform.scale(scale: v, child: child),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          customBorder: const CircleBorder(),
          onTap: onTap,
          child: Container(
            width: size,
            height: size,
            decoration: BoxDecoration(
              color: color,
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  blurRadius: 22,
                  offset: const Offset(0, 10),
                  color: Colors.black.withOpacity(0.18),
                ),
              ],
            ),
            child: Center(
              child:
                  item.customIcon != null
                      ? ClipOval(
                        child: SizedBox(
                          width: size,
                          height: size,
                          child: item.customIcon,
                        ),
                      )
                      : Icon(item.icon, color: Colors.white, size: 26),
            ),
          ),
        ),
      ),
    );
  }
}

class _NotchClipper extends CustomClipper<Path> {
  _NotchClipper({required this.notchCenterX, required this.notchRadius});

  final double notchCenterX;
  final double notchRadius;

  @override
  Path getClip(Size size) {
    final path = Path();
    final w = size.width;
    final h = size.height;

    final depth = notchRadius * 0.85;
    final left = (notchCenterX - notchRadius).clamp(0.0, w);
    final right = (notchCenterX + notchRadius).clamp(0.0, w);

    path.moveTo(0, 0);
    path.lineTo(left - 18, 0);
    path.cubicTo(left - 6, 0, left - 6, depth, notchCenterX, depth);
    path.cubicTo(right + 6, depth, right + 6, 0, right + 18, 0);
    path.lineTo(w, 0);
    path.lineTo(w, h);
    path.lineTo(0, h);
    path.close();
    return path;
  }

  @override
  bool shouldReclip(covariant _NotchClipper oldClipper) {
    return oldClipper.notchCenterX != notchCenterX ||
        oldClipper.notchRadius != notchRadius;
  }
}
