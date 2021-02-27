let localVideo = document.getElementById("local-video")
let remoteVideo = document.getElementById("remote-video")

//video 재생 중이 아니라면 투명도를 0으로 설정하여 보이지 않도록 함.
localVideo.style.opacity = 0
remoteVideo.style.opacity = 0

//video 재생 중이라면 투명도를 1로 설정 하여 보이도록 함.
localVideo.onplaying = () => { localVideo.style.opacity = 1 }
remoteVideo.onplaying = () => { remoteVideo.style.opacity = 1 }

let peer

function init(userId) {
    peer = new Peer(userId, {
        host: '192.168.0.4',
        port: 3618,
        path: '/peerjs'
    })
    peer.on('open', () => {
        Android.onPeerConnected()
    })
    listen()
}


let localStream
/**
 * 전화를 받을수 있도록 peerjs에 콜백을 등록하는 함수
 */
function listen() {
    peer.on('call', (call) => {

        /** 
         * navigator 객체는 브라우저와 관련된 정보를 컨트롤
         * 브라우저에 대한 버전, 정보, 종류 등 관련된 정보를 제공
         *  getUserMedia() 메서드는 사용자에게 미디어 입력 장치 사용 권한을 요청
        */
        navigator.getUserMedia({
            audio: true,
            video: true
        }, (stream) => {
            localVideo.srcObject = stream
            localStream = stream

            // 전화하려는 유저에게 stream 전달
            call.answer(stream)
            //전화 연결 완료 이벤트
            call.on('stream', (remoteStream) => {
                remoteVideo.srcObject = remoteStream
                //전화가 연결되면 큰 화면은 remote video, 작은 화면은 local video가 되도록 구현
                remoteVideo.className = "primary-video"
                localVideo.className = "secondary-video"
            })
        })
    })
}

/**
 * 상대방의 id와 stream을 peerjs로 전달하여 전화를 걸수 있도록 하는 메소드
 */
function startCall(otherUserId) {
    navigator.getUserMedia({
        audio: true,
        video: true
    }, (stream) => {
        localVideo.srcObject = stream
        localStream = stream
        const call = peer.call(otherUserId, stream)

        //전화 연결 완료 이벤트
        call.on('stream', (remoteStream) => {
            remoteVideo.srcObject = remoteStream
            //전화가 연결되면 큰 화면은 remote video, 작은 화면은 local video가 되도록 구현
            remoteVideo.className = "primary-video"
            localVideo.className = "secondary-video"
        })
    })
}

/**
 * Video를 보여줄지 토글 버튼형태로 동작하는 메소드
 * 안드로이드와 연결되는 bridge 메소드
 * @param {*} b 안드로이드 app에서 전달받는 플래그
 */
function toggleVideo(b) {
    if (b == "true") {
        localStream.getVideoTracks()[0].enabled = true
    } else {
        localStream.getVideoTracks()[0].enabled = false
    }
}

/**
 * 음소거를 할지 토글 버튼형태로 동작하는 메소드
 * 안드로이드와 연결되는 bridge 메소드
 * @param {*} b 안드로이드 app에서 전달받는 플래그
 */
function toggleAudio(b) {
    if (b == "true") {
        localStream.getAudioTracks()[0].enabled = true
    } else {
        localStream.getAudioTracks()[0].enabled = false
    }
}
