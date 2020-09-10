package org.spectral.asm.processor

import javax.lang.model.element.Element

class ProcessingException(val element: Element, message: String) : RuntimeException(message)