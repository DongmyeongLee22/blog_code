module.exports = {
    base: '/blog_code/', // base url을 설정합니다.
    title: 'VuePress 블로그',
    head: [['link', {rel: 'icon', href: 'img.png'}]], // html head에 넣을 값들을 설정할 수 있습니다.
    themeConfig: { // VuePress 기본 테마에 필요한 설정을을 추가합니다.
        logo: '/vue.png',
        nav: [
            {text: 'Home', link: '/'},
            {text: 'Sample', link: '/sample.html'},
        ]
    },
};