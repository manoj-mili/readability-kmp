import SwiftUI
import UIKit
import SharedUI

struct ContentView: View {
    var body: some View {
        ReadabilityComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

struct ReadabilityComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
